package com.example.evcharging.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients
@SpringBootApplication(scanBasePackages = "com.example.evcharging")
public class ChargingPaymentApplication {
    public static void main(String[] args) { SpringApplication.run(ChargingPaymentApplication.class, args); }
}
