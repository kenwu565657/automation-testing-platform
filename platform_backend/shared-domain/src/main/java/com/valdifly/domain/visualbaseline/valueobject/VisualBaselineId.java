package com.valdifly.domain.visualbaseline.valueobject;

import com.valdifly.domain.common.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record VisualBaselineId(String value) implements ValueObject<String> {
    public VisualBaselineId {
        Objects.requireNonNull(value, "VisualBaselineId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("VisualBaselineId cannot be blank");
        }
    }

    public static VisualBaselineId generate() {
        return new VisualBaselineId(UUID.randomUUID().toString());
    }

    public static VisualBaselineId of(String value) {
        return new VisualBaselineId(value);
    }
}
