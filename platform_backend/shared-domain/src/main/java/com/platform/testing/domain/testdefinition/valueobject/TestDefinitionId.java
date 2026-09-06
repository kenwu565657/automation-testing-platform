package com.platform.testing.domain.testdefinition.valueobject;

import com.platform.testing.domain.common.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record TestDefinitionId(String value) implements ValueObject<String> {
    public TestDefinitionId {
        Objects.requireNonNull(value, "TestDefinitionId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("TestDefinitionId cannot be blank");
        }
    }

    public static TestDefinitionId generate() {
        return new TestDefinitionId(UUID.randomUUID().toString());
    }

    public static TestDefinitionId of(String value) {
        return new TestDefinitionId(value);
    }
}
