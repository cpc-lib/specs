package com.example.evcharging.core.billing.domain;

import java.time.Instant;

public record MeterPoint(Instant occurredAt, long meterWh) {
    public MeterPoint {
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt is required");
        if (meterWh < 0) throw new IllegalArgumentException("meter cannot be negative");
    }
}
