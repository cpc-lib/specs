package com.example.evcharging.framework.contract;

import java.time.Instant;
import java.util.Map;

public record DeviceCommandMessage(
        long tenantId,
        String commandId,
        String deviceId,
        String commandType,
        Instant expireAt,
        Map<String, Object> payload
) {}
