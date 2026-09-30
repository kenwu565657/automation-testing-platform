package com.valdifly.domain.common;

import java.time.Instant;

public interface DomainEvent {
    String eventType();

    Instant occurredAt();
}
