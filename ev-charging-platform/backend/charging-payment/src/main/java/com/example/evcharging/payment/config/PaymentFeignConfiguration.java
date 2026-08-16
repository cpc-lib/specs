package com.example.evcharging.payment.config;

import com.example.evcharging.framework.context.RequestContext;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class PaymentFeignConfiguration {
  @Bean
  RequestInterceptor requestContextFeignInterceptor(
      @Value("${charging.security.internal-service-key:${INTERNAL_SERVICE_KEY:dev-internal-service-key-change-me}}") String serviceKey) {
    return template -> {
      Long tenant=RequestContext.tenantId();
      if(tenant!=null){
        template.header("X-Service-Key",serviceKey);
        template.header("X-Internal-Tenant-Id",String.valueOf(tenant));
        RequestContext.currentUserId().ifPresent(user->template.header("X-Internal-User-Id",String.valueOf(user)));
      }
      String requestId=RequestContext.requestId();
      if(requestId!=null&&!requestId.isBlank())template.header("X-Request-Id",requestId);
    };
  }
}
