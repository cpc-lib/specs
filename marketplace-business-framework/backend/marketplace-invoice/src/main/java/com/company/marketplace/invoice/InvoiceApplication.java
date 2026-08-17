package com.company.marketplace.invoice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Invoice application, issue, red flush, reissue and delivery.
 *
 * V1.1 contains framework/skeleton only. Business behavior is implemented
 * incrementally from Marketplace V3.0 SPEC.
 */
@SpringBootApplication
public class InvoiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvoiceApplication.class, args);
    }
}
