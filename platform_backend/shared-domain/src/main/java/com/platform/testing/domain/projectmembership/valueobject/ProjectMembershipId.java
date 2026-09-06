package com.platform.testing.domain.projectmembership.valueobject;

import com.platform.testing.domain.common.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record ProjectMembershipId(String value) implements ValueObject<String> {
    public ProjectMembershipId {
        Objects.requireNonNull(value, "ProjectMembershipId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ProjectMembershipId cannot be blank");
        }
    }

    public static ProjectMembershipId generate() {
        return new ProjectMembershipId(UUID.randomUUID().toString());
    }

    public static ProjectMembershipId of(String value) {
        return new ProjectMembershipId(value);
    }
}
