package com.company.marketplace.cqrs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CQRS read models such as Buyer360 and denormalized marketplace views.
 *
 * V1.1 contains framework/skeleton only. Business behavior is implemented
 * incrementally from Marketplace V3.0 SPEC.
 */
@SpringBootApplication
public class CqrsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CqrsApplication.class, args);
    }
}
