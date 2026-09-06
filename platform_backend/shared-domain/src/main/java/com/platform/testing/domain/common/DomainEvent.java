package com.platform.testing.domain.common;

import java.time.Instant;

public interface DomainEvent {
    String eventType();

    Instant occurredAt();
}
