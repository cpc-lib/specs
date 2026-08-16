package com.example.evcharging.core.config;

import com.example.evcharging.framework.id.IdGenerator;
import com.example.evcharging.framework.id.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreInfrastructureConfiguration {
    @Bean
    IdGenerator idGenerator(
            @Value("${charging.id.datacenter-id:1}") long datacenterId,
            @Value("${charging.id.worker-id:2}") long workerId) {
        return new SnowflakeIdGenerator(datacenterId, workerId);
    }
}
