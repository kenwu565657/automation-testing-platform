package com.valdifly.domain.testcase.valueobject;

import com.valdifly.domain.common.ValueObject;
import java.util.Objects;
import java.util.UUID;

/**
 * DDD Value Object: TestCase identity.
 * Immutable, equality by value.
 */
public record TestCaseId(String value) implements ValueObject<String> {

    public TestCaseId {
        Objects.requireNonNull(value, "TestCaseId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("TestCaseId cannot be blank");
        }
    }

    public static TestCaseId generate() {
        return new TestCaseId(UUID.randomUUID().toString());
    }

    public static TestCaseId of(String value) {
        return new TestCaseId(value);
    }
}
