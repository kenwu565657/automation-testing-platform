package com.platform.testing.domain.target.valueobject;

import com.platform.testing.domain.common.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record ExecutionTargetId(String value) implements ValueObject<String> {
    public ExecutionTargetId {
        Objects.requireNonNull(value, "ExecutionTargetId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ExecutionTargetId cannot be blank");
        }
    }

    public static ExecutionTargetId generate() {
        return new ExecutionTargetId(UUID.randomUUID().toString());
    }

    public static ExecutionTargetId of(String value) {
        return new ExecutionTargetId(value);
    }
}
