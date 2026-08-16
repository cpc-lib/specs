package com.example.evcharging.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableScheduling
@EnableFeignClients
@SpringBootApplication(scanBasePackages = "com.example.evcharging")
public class ChargingFinanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChargingFinanceApplication.class, args);
    }
}
