package com.company.marketplace.checkout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Checkout session, quote and authoritative pre-trade validation.
 *
 * V1.1 contains framework/skeleton only. Business behavior is implemented
 * incrementally from Marketplace V3.0 SPEC.
 */
@SpringBootApplication
public class CheckoutApplication {
    public static void main(String[] args) {
        SpringApplication.run(CheckoutApplication.class, args);
    }
}
