package com.company.marketplace.framework.common.event;

import java.time.Instant;

public interface DomainEvent {
    String eventId();
    String eventType();
    String aggregateId();
    Instant occurredAt();
}
