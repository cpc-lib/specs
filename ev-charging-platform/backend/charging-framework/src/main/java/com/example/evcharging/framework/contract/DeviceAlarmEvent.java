package com.example.evcharging.framework.contract;

import java.time.Instant;

public record DeviceAlarmEvent(
        long tenantId,
        String eventType,
        String deviceId,
        Integer connectorNo,
        String alarmCode,
        String severity,
        String metricValue,
        String metricUnit,
        String message,
        Instant occurredAt
) {}
