package com.example.evcharging.payment.config;
import com.example.evcharging.framework.id.IdGenerator;
import com.example.evcharging.framework.id.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class PaymentInfrastructureConfiguration {
  @Bean IdGenerator paymentIdGenerator(@Value("${charging.id.datacenter-id:1}") long dc,@Value("${charging.id.worker-id:4}") long worker){return new SnowflakeIdGenerator(dc,worker);}
}
