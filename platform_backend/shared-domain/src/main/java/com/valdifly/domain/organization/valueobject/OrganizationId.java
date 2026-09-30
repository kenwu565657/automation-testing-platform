package com.valdifly.domain.organization.valueobject;

import com.valdifly.domain.common.ValueObject;
import java.util.Objects;
import java.util.UUID;

public record OrganizationId(String value) implements ValueObject<String> {
    public OrganizationId {
        Objects.requireNonNull(value, "OrganizationId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("OrganizationId cannot be blank");
        }
    }

    public static OrganizationId generate() {
        return new OrganizationId(UUID.randomUUID().toString());
    }

    public static OrganizationId of(String value) {
        return new OrganizationId(value);
    }
}
