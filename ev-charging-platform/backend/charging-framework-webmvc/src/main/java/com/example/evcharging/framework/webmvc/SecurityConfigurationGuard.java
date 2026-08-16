package com.example.evcharging.framework.webmvc;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecurityConfigurationGuard implements InitializingBean {
    private final String appEnv;
    private final boolean devHeaders;
    private final String accessSecret;
    private final String serviceKey;

    public SecurityConfigurationGuard(
            @Value("${APP_ENV:dev}") String appEnv,
            @Value("${charging.security.dev-tenant-header-enabled:${DEV_TENANT_HEADER_ENABLED:false}}") boolean devHeaders,
            @Value("${charging.security.access-token-secret:${ACCESS_TOKEN_SECRET:dev-access-token-secret-change-me-1234567890}}") String accessSecret,
            @Value("${charging.security.internal-service-key:${INTERNAL_SERVICE_KEY:dev-internal-service-key-change-me}}") String serviceKey){
        this.appEnv=appEnv;this.devHeaders=devHeaders;this.accessSecret=accessSecret;this.serviceKey=serviceKey;
    }

    @Override
    public void afterPropertiesSet(){
        if(!"prod".equalsIgnoreCase(appEnv)&&!"production".equalsIgnoreCase(appEnv)) return;
        if(devHeaders) throw new IllegalStateException("development identity headers must be disabled in production");
        if(accessSecret==null||accessSecret.length()<32||accessSecret.startsWith("dev-access-token-secret"))
            throw new IllegalStateException("production ACCESS_TOKEN_SECRET is not securely configured");
        if(serviceKey==null||serviceKey.length()<32||serviceKey.startsWith("dev-internal-service-key"))
            throw new IllegalStateException("production INTERNAL_SERVICE_KEY is not securely configured");
        if(accessSecret.equals(serviceKey))
            throw new IllegalStateException("access-token secret and internal-service key must be different");
    }
}
