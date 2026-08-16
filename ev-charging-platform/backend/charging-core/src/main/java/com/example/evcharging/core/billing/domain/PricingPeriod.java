package com.example.evcharging.core.billing.domain;

public record PricingPeriod(
        int sequence,
        String periodType,
        int startMinute,
        int endMinute,
        long energyPriceMicro,
        long servicePriceMicro
) {
    public PricingPeriod {
        if (startMinute < 0 || startMinute >= 1440 || endMinute <= 0 || endMinute > 1440 || startMinute >= endMinute) {
            throw new IllegalArgumentException("pricing period must be a non-wrapping [start,end) interval inside one day");
        }
        if (energyPriceMicro < 0 || servicePriceMicro < 0) throw new IllegalArgumentException("negative price");
    }

    public boolean containsMinute(int minute) {
        return minute >= startMinute && minute < endMinute;
    }
}
