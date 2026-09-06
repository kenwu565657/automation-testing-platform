package com.platform.testing.domain.common;

import com.platform.testing.utils.TimeUtils;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Metadata attached to every domain event.
 * Enables tracing, ordering, and source identification.
 */
public record EventMetadata(
        String eventId,
        String eventType,
        String sourceService,
        String correlationId,
        Instant timestamp
) {
    public EventMetadata {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(eventType);
        Objects.requireNonNull(sourceService);
        if (timestamp == null) timestamp = TimeUtils.now();
        if (correlationId == null) correlationId = eventId;
    }

    public static EventMetadata create(String eventType, String sourceService, String correlationId) {
        return new EventMetadata(
                UUID.randomUUID().toString(),
                eventType,
                sourceService,
                correlationId,
                TimeUtils.now()
        );
    }

    public static EventMetadata create(String eventType, String sourceService) {
        String id = UUID.randomUUID().toString();
        return new EventMetadata(id, eventType, sourceService, id, TimeUtils.now());
    }
}
