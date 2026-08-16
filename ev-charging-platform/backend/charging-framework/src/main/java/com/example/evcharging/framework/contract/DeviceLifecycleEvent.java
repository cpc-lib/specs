package com.example.evcharging.framework.contract;

import java.time.Instant;

public record DeviceLifecycleEvent(
        long tenantId,
        String eventType,
        String deviceId,
        String gatewayId,
        String connectionToken,
        String reason,
        Instant occurredAt
) {}
