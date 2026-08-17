package com.company.marketplace.merchant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Merchant onboarding, verification, qualification, deposit, credit and exit.
 *
 * V1.1 contains framework/skeleton only. Business behavior is implemented
 * incrementally from Marketplace V3.0 SPEC.
 */
@SpringBootApplication
public class MerchantApplication {
    public static void main(String[] args) {
        SpringApplication.run(MerchantApplication.class, args);
    }
}
