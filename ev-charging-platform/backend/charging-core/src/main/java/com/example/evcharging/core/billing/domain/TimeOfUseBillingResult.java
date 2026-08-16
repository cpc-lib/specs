package com.example.evcharging.core.billing.domain;

import java.util.List;

public record TimeOfUseBillingResult(
        long energyWh,
        long energyAmountFen,
        long serviceAmountFen,
        long parkingAmountFen,
        long occupationAmountFen,
        long discountAmountFen,
        long receivableAmountFen,
        List<BillingSegment> segments
) {}
