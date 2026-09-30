package com.valdifly.domain.organizationmembership.valueobject;

import com.valdifly.domain.common.ValueObject;
import java.util.Objects;
import java.util.UUID;

public record OrganizationMembershipId(String value) implements ValueObject<String> {
    public OrganizationMembershipId {
        Objects.requireNonNull(value, "OrganizationMembershipId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("OrganizationMembershipId cannot be blank");
        }
    }

    public static OrganizationMembershipId generate() {
        return new OrganizationMembershipId(UUID.randomUUID().toString());
    }

    public static OrganizationMembershipId of(String value) {
        return new OrganizationMembershipId(value);
    }
}
