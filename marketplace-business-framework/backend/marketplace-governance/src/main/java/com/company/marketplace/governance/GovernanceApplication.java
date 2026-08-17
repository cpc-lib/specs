package com.company.marketplace.governance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Violation, penalty, appeal, prohibited goods and IP/counterfeit governance.
 *
 * V1.1 contains framework/skeleton only. Business behavior is implemented
 * incrementally from Marketplace V3.0 SPEC.
 */
@SpringBootApplication
public class GovernanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GovernanceApplication.class, args);
    }
}
