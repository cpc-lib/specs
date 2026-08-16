package com.example.evcharging.core.billing.domain;
import java.time.Instant; import java.time.ZoneId;
public record BillingContext(ZoneId stationZoneId, Instant chargingStartTime, Instant chargingEndTime, long startMeterWh, long endMeterWh, String billingSnapshotJson) {}
