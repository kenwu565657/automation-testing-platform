package com.valdifly.domain.pageobject.valueobject;

import com.valdifly.domain.common.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record PageObjectId(String value) implements ValueObject<String> {
    public PageObjectId {
        Objects.requireNonNull(value);
        if (value.isBlank()) {
            throw new IllegalArgumentException("PageObjectId cannot be blank");
        }
    }
    public static PageObjectId generate() {
        return new PageObjectId(UUID.randomUUID().toString());
    }

    public static PageObjectId of(String value) {
        return new PageObjectId(value);
    }
}
