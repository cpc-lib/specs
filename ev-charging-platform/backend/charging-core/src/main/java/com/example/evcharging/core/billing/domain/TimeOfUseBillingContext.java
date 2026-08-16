package com.example.evcharging.core.billing.domain;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

public record TimeOfUseBillingContext(
        ZoneId stationZoneId,
        Instant chargingStartTime,
        Instant chargingEndTime,
        long startMeterWh,
        long endMeterWh,
        List<MeterPoint> meterPoints,
        List<PricingPeriod> periods
) {}
