package com.company.marketplace.recommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Recommendation candidates, model/policy versions and experiments.
 *
 * V1.1 contains framework/skeleton only. Business behavior is implemented
 * incrementally from Marketplace V3.0 SPEC.
 */
@SpringBootApplication
public class RecommendationApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecommendationApplication.class, args);
    }
}
