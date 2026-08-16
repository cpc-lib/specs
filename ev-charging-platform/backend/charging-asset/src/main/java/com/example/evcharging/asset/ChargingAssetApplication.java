package com.example.evcharging.asset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.example.evcharging")
public class ChargingAssetApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChargingAssetApplication.class, args);
    }
}
