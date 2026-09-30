package com.valdifly.domain.execution.valueobject;

import com.valdifly.domain.common.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record TestRunId(String value) implements ValueObject<String> {
    public TestRunId {
        Objects.requireNonNull(value);
        if (value.isBlank()) {
            throw new IllegalArgumentException("TestRunId cannot be blank");
        }
    }

    public static TestRunId generate() {
        return new TestRunId(UUID.randomUUID().toString());
    }

    public static TestRunId of(String value) {
        return new TestRunId(value);
    }
}
