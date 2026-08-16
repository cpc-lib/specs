package com.example.evcharging.finance.config;
import com.example.evcharging.framework.id.IdGenerator;import com.example.evcharging.framework.id.SnowflakeIdGenerator;import org.springframework.beans.factory.annotation.Value;import org.springframework.context.annotation.*;
@Configuration public class FinanceInfrastructureConfiguration {@Bean IdGenerator financeIdGenerator(@Value("${charging.id.datacenter-id:1}")long dc,@Value("${charging.id.worker-id:5}")long worker){return new SnowflakeIdGenerator(dc,worker);}}
