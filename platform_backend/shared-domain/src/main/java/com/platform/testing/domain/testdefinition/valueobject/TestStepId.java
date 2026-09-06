package com.platform.testing.domain.testdefinition.valueobject;

import com.platform.testing.domain.common.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record TestStepId(String value) implements ValueObject<String> {
    public TestStepId {
        Objects.requireNonNull(value, "TestStepId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("TestStepId cannot be blank");
        }
    }

    public static TestStepId generate() {
        return new TestStepId(UUID.randomUUID().toString());
    }

    public static TestStepId of(String value) {
        return new TestStepId(value);
    }
}
