package com.example.evcharging.open;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients
@SpringBootApplication(scanBasePackages="com.example.evcharging")
public class ChargingOpenApplication {
    public static void main(String[] args){SpringApplication.run(ChargingOpenApplication.class,args);}
}
