package com.valdifly.domain.project;

import com.valdifly.domain.common.AggregateRoot;
import com.valdifly.domain.organization.valueobject.OrganizationId;
import com.valdifly.domain.project.valueobject.ProjectId;
import com.valdifly.utils.TimeUtils;

import java.time.Instant;
import java.util.Objects;

public class Project implements AggregateRoot<ProjectId> {
    private final ProjectId id;
    private final OrganizationId organizationId;
    private String name;
    private final String description;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static Project create(String name, String description) {
        return create(name, description, null);
    }

    public static Project create(String name, String description, OrganizationId organizationId) {
        return new Project(ProjectId.generate(), name, description, organizationId);
    }

    public static Project reconstitute(
            ProjectId id,
            String name,
            String description,
            OrganizationId organizationId,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        Project p = new Project(id, name, description, organizationId);
        p.active = active;
        p.createdAt = createdAt;
        p.updatedAt = updatedAt;
        return p;
    }

    public static Project reconstitute(ProjectId id, String name, String description,
                                       boolean active, Instant createdAt, Instant updatedAt) {
        return reconstitute(id, name, description, null, active, createdAt, updatedAt);
    }

    private Project(ProjectId id, String name, String description, OrganizationId organizationId) {
        this.id = Objects.requireNonNull(id);
        this.name = requireName(name);
        this.description = description;
        this.organizationId = organizationId;
        this.active = true;
        this.createdAt = TimeUtils.now();
        this.updatedAt = this.createdAt;
    }

    public void rename(String newName) {
        this.name = requireName(newName);
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

    public ProjectId getId() { return id; }
    public OrganizationId getOrganizationId() { return organizationId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
