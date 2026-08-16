package com.example.evcharging.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.evcharging")
public class ChargingGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChargingGatewayApplication.class, args);
    }
}
