package com.example.evcharging.operation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableScheduling
@EnableFeignClients
@SpringBootApplication(scanBasePackages = "com.example.evcharging")
public class ChargingOperationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChargingOperationApplication.class, args);
    }
}
