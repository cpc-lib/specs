package com.example.evcharging.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.evcharging")
public class ChargingSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChargingSystemApplication.class, args);
    }
}
