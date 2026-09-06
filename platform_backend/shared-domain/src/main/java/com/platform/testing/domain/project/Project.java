package com.platform.testing.domain.project;

import com.platform.testing.domain.common.AggregateRoot;
import com.platform.testing.domain.project.valueobject.ProjectId;
import com.platform.testing.utils.TimeUtils;

import java.time.Instant;
import java.util.Objects;

public class Project implements AggregateRoot<ProjectId> {
    private final ProjectId id;
    private String name;
    private final String description;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static Project create(String name, String description) {
        return new Project(ProjectId.generate(), name, description);
    }

    public static Project reconstitute(ProjectId id, String name, String description,
                                       boolean active, Instant createdAt, Instant updatedAt) {
        Project p = new Project(id, name, description);
        p.active = active;
        p.createdAt = createdAt;
        p.updatedAt = updatedAt;
        return p;
    }

    private Project(ProjectId id, String name, String description) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.active = true;
        this.createdAt = TimeUtils.now();
        this.updatedAt = this.createdAt;
    }

    public void rename(String newName) {
        this.name = Objects.requireNonNull(newName);
        touch();
    }

    public void deactivate() {
        this.active = false;
        touch();
    }

    private void touch() {
        this.updatedAt = TimeUtils.now();
    }

    public ProjectId getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
