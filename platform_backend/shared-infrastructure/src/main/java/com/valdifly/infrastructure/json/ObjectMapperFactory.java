package com.valdifly.infrastructure.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Single factory for a consistent Jackson ObjectMapper across all services.
 *
 * Every service (Spring Boot, Vert.x, Ktor) should use this ObjectMapper
 * for Kafka serialization/deserialization so events are always compatible.
 *
 * Usage:
 *   ObjectMapper mapper = ObjectMapperFactory.create();
 */
public final class ObjectMapperFactory {

    private ObjectMapperFactory() {}

    private static final ObjectMapper INSTANCE = configure(new ObjectMapper());

    /**
     * Returns a shared singleton instance.
     * Safe to use across threads (ObjectMapper is thread-safe after configuration).
     */
    public static ObjectMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a new configured ObjectMapper instance.
     * Use this if you need to customize further without affecting the singleton.
     */
    public static ObjectMapper create() {
        return configure(new ObjectMapper());
    }

    private static ObjectMapper configure(ObjectMapper mapper) {
        // Java 8+ time support (Instant, LocalDateTime, etc.)
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new ValueObjectModule());

        // Write Instant as ISO-8601 string, not numeric timestamp
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Don't fail on unknown properties (forward compatibility)
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // Omit null fields in JSON output
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Use camelCase (Java default) — no snake_case
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);

        return mapper;
    }
}
