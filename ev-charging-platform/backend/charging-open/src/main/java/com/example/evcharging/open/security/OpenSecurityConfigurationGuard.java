package com.example.evcharging.open.security;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class OpenSecurityConfigurationGuard implements InitializingBean {
    private final String appEnv;private final String masterKeyBase64;

    public OpenSecurityConfigurationGuard(
            @Value("${APP_ENV:dev}") String appEnv,
            @Value("${charging.open.master-key-base64}") String masterKeyBase64){
        this.appEnv=appEnv;this.masterKeyBase64=masterKeyBase64;
    }

    @Override public void afterPropertiesSet(){
        byte[] key;
        try{key=Base64.getDecoder().decode(masterKeyBase64);}
        catch(Exception e){throw new IllegalStateException("OPENAPI master key is invalid",e);}
        if(key.length!=32)throw new IllegalStateException("OPENAPI master key must be 32 bytes");
        if(("prod".equalsIgnoreCase(appEnv)||"production".equalsIgnoreCase(appEnv))
                && new String(key,java.nio.charset.StandardCharsets.UTF_8).startsWith("dev-openapi-master"))
            throw new IllegalStateException("production OPENAPI master key must not use the development default");
    }
}
