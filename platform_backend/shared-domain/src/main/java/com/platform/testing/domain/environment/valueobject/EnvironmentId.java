package com.platform.testing.domain.environment.valueobject;

import com.platform.testing.domain.common.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record EnvironmentId(String value) implements ValueObject<String> {
    public EnvironmentId {
        Objects.requireNonNull(value);
        if (value.isBlank()) {
            throw new IllegalArgumentException("EnvironmentId cannot be blank");
        }
    }
    public static EnvironmentId generate() {
        return new EnvironmentId(UUID.randomUUID().toString());
    }

    public static EnvironmentId of(String value) {
        return new EnvironmentId(value);
    }
}
