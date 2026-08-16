package com.example.evcharging.framework.webmvc;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class ProductWebMvcConfiguration implements WebMvcConfigurer {
    private final PermissionInterceptor permissionInterceptor;
    public ProductWebMvcConfiguration(PermissionInterceptor permissionInterceptor){this.permissionInterceptor=permissionInterceptor;}
    @Override public void addInterceptors(InterceptorRegistry registry){registry.addInterceptor(permissionInterceptor);}
}
