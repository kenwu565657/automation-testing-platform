package com.platform.testing.domain.project.valueobject;

import com.platform.testing.domain.common.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record ProjectId(String value) implements ValueObject<String> {
    public ProjectId {
        Objects.requireNonNull(value, "ProjectId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ProjectId cannot be blank");
        }
    }
    public static ProjectId generate() {
        return new ProjectId(UUID.randomUUID().toString());
    }

    public static ProjectId of(String value) {
        return new ProjectId(value);
    }
}
