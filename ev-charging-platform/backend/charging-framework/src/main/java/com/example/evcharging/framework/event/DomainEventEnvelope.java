package com.example.evcharging.framework.event;

import java.time.Instant;

public record DomainEventEnvelope<T>(
        String eventId,
        String eventType,
        String eventVersion,
        String aggregateType,
        String aggregateId,
        long tenantId,
        String traceId,
        Instant occurredAt,
        String producer,
        T payload
) {}
