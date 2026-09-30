package com.valdifly.domain.organization;

import com.valdifly.domain.common.AggregateRoot;
import com.valdifly.domain.organization.valueobject.OrganizationId;
import com.valdifly.domain.projectmembership.valueobject.ResourceArn;
import com.valdifly.domain.user.valueobject.UserId;
import com.valdifly.utils.TimeUtils;
import java.time.Instant;
import java.util.Objects;

public class Organization implements AggregateRoot<OrganizationId> {

    private final OrganizationId id;
    private String name;
    private String description;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private final UserId createdBy;

    public static Organization create(String name, String description, UserId createdBy) {
        return new Organization(OrganizationId.generate(), name, description, createdBy);
    }

    public static Organization reconstitute(
            OrganizationId id,
            String name,
            String description,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            UserId createdBy
    ) {
        Organization organization = new Organization(id, name, description, createdBy);
        organization.active = active;
        organization.createdAt = createdAt;
        organization.updatedAt = updatedAt;
        return organization;
    }

    private Organization(OrganizationId id, String name, String description, UserId createdBy) {
        this.id = Objects.requireNonNull(id);
        this.name = requireName(name);
        this.description = description;
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy is required");
        this.active = true;
        this.createdAt = TimeUtils.now();
        this.updatedAt = this.createdAt;
    }

    public void rename(String name) {
        this.name = requireName(name);
        touch();
    }

    public void changeDescription(String description) {
        this.description = description;
        touch();
    }

    public void deactivate() {
        this.active = false;
        touch();
    }

    public void activate() {
        this.active = true;
        touch();
    }

    public ResourceArn resourceArn() {
        return ResourceArn.organization(id);
    }

    private void touch() {
        this.updatedAt = TimeUtils.now();
    }

    private static String requireName(String name) {
        Objects.requireNonNull(name, "name is required");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        return name;
    }

    @Override
    public OrganizationId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public UserId getCreatedBy() {
        return createdBy;
    }
}
