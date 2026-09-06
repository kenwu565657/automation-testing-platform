package com.platform.testing.domain.testsuite.valueobject;

import com.platform.testing.domain.common.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record TestSuiteId(String value) implements ValueObject<String> {
    public TestSuiteId {
        Objects.requireNonNull(value, "TestSuiteId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("TestSuiteId cannot be blank");
        }
    }
    public static TestSuiteId generate() {
        return new TestSuiteId(UUID.randomUUID().toString());
    }

    public static TestSuiteId of(String value) {
        return new TestSuiteId(value);
    }
}
