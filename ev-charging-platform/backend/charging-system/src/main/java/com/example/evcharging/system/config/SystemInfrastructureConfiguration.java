package com.example.evcharging.system.config;

import com.example.evcharging.framework.id.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class SystemInfrastructureConfiguration {
    @Bean
    IdGenerator systemIdGenerator(
            @Value("${charging.id.datacenter-id:1}") long datacenterId,
            @Value("${charging.id.worker-id:1}") long workerId) {
        return new SnowflakeIdGenerator(datacenterId,workerId);
    }
}
