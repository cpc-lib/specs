package com.example.evcharging.open.config;

import com.example.evcharging.framework.id.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class OpenInfrastructureConfiguration {
    @Bean
    IdGenerator openIdGenerator(
            @Value("${charging.id.datacenter-id:1}") long datacenterId,
            @Value("${charging.id.worker-id:8}") long workerId){
        return new SnowflakeIdGenerator(datacenterId,workerId);
    }
}
