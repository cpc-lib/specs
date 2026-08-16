package com.company.marketplace.dispute;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Dispute evidence, arbitration decision and domain-command execution.
 *
 * V1.1 contains framework/skeleton only. Business behavior is implemented
 * incrementally from Marketplace V3.0 SPEC.
 */
@SpringBootApplication
public class DisputeApplication {
    public static void main(String[] args) {
        SpringApplication.run(DisputeApplication.class, args);
    }
}
