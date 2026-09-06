package com.platform.testing.domain.pageobject.valueobject;

import com.platform.testing.domain.common.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record PageElementId(String value) implements ValueObject<String> {
    public PageElementId {
        Objects.requireNonNull(value, "PageElementId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("PageElementId cannot be blank");
        }
    }

    public static PageElementId generate() {
        return new PageElementId(UUID.randomUUID().toString());
    }

    public static PageElementId of(String value) {
        return new PageElementId(value);
    }
}
