package com.example.evcharging.open.config;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.open.security.PartnerContext;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class OpenFeignConfiguration {
    @Bean
    RequestInterceptor openFeignContext(
            @Value("${charging.security.internal-service-key:${INTERNAL_SERVICE_KEY:dev-internal-service-key-change-me}}") String serviceKey){
        return template->{
            Long tenant=null;
            var partner=PartnerContext.current();
            if(partner!=null)tenant=partner.tenantId();
            else tenant=RequestContext.tenantId();
            if(tenant!=null){
                template.header("X-Service-Key",serviceKey);
                template.header("X-Internal-Tenant-Id",String.valueOf(tenant));
                RequestContext.currentUserId().ifPresent(u->template.header("X-Internal-User-Id",String.valueOf(u)));
            }
            String requestId=RequestContext.requestId();
            if(requestId!=null&&!requestId.isBlank())template.header("X-Request-Id",requestId);
        };
    }
}
