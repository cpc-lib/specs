package com.example.evcharging.core.billing.domain;

import java.time.Instant;

public record BillingSegment(
        int sequence,
        String periodType,
        Instant start,
        Instant end,
        long startMeterWh,
        long endMeterWh,
        long energyWh,
        long energyPriceMicro,
        long servicePriceMicro,
        long energyAmountFen,
        long serviceAmountFen
) {}
