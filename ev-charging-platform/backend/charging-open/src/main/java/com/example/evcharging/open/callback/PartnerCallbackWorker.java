package com.example.evcharging.open.callback;

import org.springframework.stereotype.Service;
import com.example.evcharging.open.security.OutboundUrlPolicy;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.*;

@Service
public class PartnerCallbackWorker {
    private final PartnerCallbackTaskRepository tasks;private final OutboundUrlPolicy outbound;
    private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public PartnerCallbackWorker(PartnerCallbackTaskRepository tasks,OutboundUrlPolicy outbound){this.tasks=tasks;this.outbound=outbound;}

    public void send(long id){
        var task=tasks.claim(id);if(task==null)return;
        if(task.callbackUrl()==null||task.callbackSecret()==null){
            tasks.failure(task,"partner callback is not configured",null,null);return;
        }
        try{
            byte[] body=task.payloadJson().getBytes(StandardCharsets.UTF_8);
            var signed=PartnerCallbackSigner.sign(task.callbackSecret(),body,Instant.now().getEpochSecond());
            HttpRequest request=HttpRequest.newBuilder(outbound.requireAllowed(task.callbackUrl()))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type","application/json")
                    .header("X-Callback-Timestamp",signed.timestamp())
                    .header("X-Callback-Nonce",signed.nonce())
                    .header("X-Callback-Body-SHA256",signed.bodySha256())
                    .header("X-Callback-Signature-Version","v1")
                    .header("X-Callback-Signature",signed.signature())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();
            HttpResponse<String> response=http.send(request,HttpResponse.BodyHandlers.ofString());
            if(response.statusCode()>=200&&response.statusCode()<300)
                tasks.success(task,response.statusCode(),response.body());
            else tasks.failure(task,"HTTP "+response.statusCode(),response.statusCode(),response.body());
        }catch(Exception e){
            tasks.failure(task,e.getClass().getSimpleName()+": "+e.getMessage(),null,null);
        }
    }
}
