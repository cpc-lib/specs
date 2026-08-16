package com.example.evcharging.iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.example.evcharging")
public class ChargingIotApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChargingIotApplication.class, args);
    }
}
