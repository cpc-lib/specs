package com.example.evcharging.open.regulatory;

import org.springframework.stereotype.Service;
import com.example.evcharging.open.security.OutboundUrlPolicy;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.List;

@Service
public class RegulatoryDispatchWorker {
    private final RegulatoryTaskRepository tasks;private final List<RegulatoryProtocolAdapter> adapters;
    private final RegulatoryRateLimiter rateLimiter;private final OutboundUrlPolicy outbound;
    private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public RegulatoryDispatchWorker(RegulatoryTaskRepository tasks,List<RegulatoryProtocolAdapter> adapters,
                                    RegulatoryRateLimiter rateLimiter,OutboundUrlPolicy outbound){
        this.tasks=tasks;this.adapters=adapters;this.rateLimiter=rateLimiter;this.outbound=outbound;
    }

    public void send(long id){
        var task=tasks.claim(id);if(task==null)return;
        try{
            RegulatoryProtocolAdapter adapter=adapters.stream().filter(a->a.supports(task.protocolCode()))
                    .findFirst().orElseThrow(()->new IllegalStateException("regulatory adapter missing: "+task.protocolCode()));
            rateLimiter.require(task.platformId(),task.rateLimitPerMinute());
            var platform=new RegulatoryProtocolAdapter.Platform(task.platformId(),task.tenantId(),task.platformCode(),
                    task.endpointUrl(),task.credentialKey(),task.credentialSecret());
            var prepared=adapter.prepare(platform,task.dataType(),task.businessKey(),task.sourcePayloadJson());

            HttpRequest.Builder builder=HttpRequest.newBuilder(outbound.requireAllowed(prepared.endpointUrl()))
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(prepared.body()));
            prepared.headers().forEach(builder::header);
            HttpResponse<String> response=http.send(builder.build(),HttpResponse.BodyHandlers.ofString());
            if(response.statusCode()>=200&&response.statusCode()<300)
                tasks.success(task,response.statusCode(),response.body());
            else tasks.failure(task,"HTTP "+response.statusCode(),response.statusCode(),response.body());
        }catch(Exception e){
            tasks.failure(task,e.getClass().getSimpleName()+": "+e.getMessage(),null,null);
        }
    }
}
