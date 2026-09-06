package com.platform.testing.domain.common;

import java.util.Objects;

/**
 * Envelope around a domain event: metadata plus payload.
 * Infrastructure serializes this when publishing.
 */
public record DomainEventEnvelope<T>(
        EventMetadata metadata,
        T payload
) {
    public DomainEventEnvelope {
        Objects.requireNonNull(metadata, "metadata is required");
        Objects.requireNonNull(payload, "payload is required");
    }

    public static <T extends DomainEvent> DomainEventEnvelope<T> wrap(T payload, String sourceService) {
        Objects.requireNonNull(payload, "payload is required");
        return new DomainEventEnvelope<>(
                EventMetadata.create(payload.eventType(), sourceService),
                payload
        );
    }

    public static <T extends DomainEvent> DomainEventEnvelope<T> wrap(
            T payload, String sourceService, String correlationId) {
        Objects.requireNonNull(payload, "payload is required");
        return new DomainEventEnvelope<>(
                EventMetadata.create(payload.eventType(), sourceService, correlationId),
                payload
        );
    }
}
