package com.valdifly.domain.execution.valueobject;

import com.valdifly.domain.common.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record TestCaseResultId(String value) implements ValueObject<String> {
    public TestCaseResultId {
        Objects.requireNonNull(value, "TestCaseResultId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("TestCaseResultId cannot be blank");
        }
    }

    public static TestCaseResultId generate() {
        return new TestCaseResultId(UUID.randomUUID().toString());
    }

    public static TestCaseResultId of(String value) {
        return new TestCaseResultId(value);
    }
}
