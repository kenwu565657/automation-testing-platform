package com.valdifly.domain.visualbaseline;

import com.valdifly.domain.common.AggregateRoot;
import com.valdifly.domain.project.valueobject.ProjectId;
import com.valdifly.domain.user.valueobject.UserId;
import com.valdifly.domain.visualbaseline.valueobject.VisualBaselineId;
import com.valdifly.utils.TimeUtils;

import java.time.Instant;
import java.util.Objects;

public class VisualBaseline implements AggregateRoot<VisualBaselineId> {

    private final VisualBaselineId id;
    private final ProjectId projectId;
    private String name;
    private String imageKey;
    private Instant createdAt;
    private Instant updatedAt;
    private final UserId createdBy;

    public static VisualBaseline create(
            VisualBaselineId id,
            ProjectId projectId,
            String name,
            String imageKey,
            UserId createdBy
    ) {
        return new VisualBaseline(id, projectId, name, imageKey, createdBy);
    }

    public static VisualBaseline reconstitute(
            VisualBaselineId id,
            ProjectId projectId,
            String name,
            String imageKey,
            Instant createdAt,
            Instant updatedAt,
            UserId createdBy
    ) {
        VisualBaseline baseline = new VisualBaseline(id, projectId, name, imageKey, createdBy);
        baseline.createdAt = createdAt;
        baseline.updatedAt = updatedAt;
        return baseline;
    }

    private VisualBaseline(
            VisualBaselineId id,
            ProjectId projectId,
            String name,
            String imageKey,
            UserId createdBy
    ) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.projectId = Objects.requireNonNull(projectId, "projectId is required");
        this.name = requireName(name);
        this.imageKey = requireKey(imageKey);
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy is required");
        this.createdAt = TimeUtils.now();
        this.updatedAt = this.createdAt;
    }

    public void replaceImage(String imageKey) {
        this.imageKey = requireKey(imageKey);
        this.updatedAt = TimeUtils.now();
    }

    public void rename(String name) {
        this.name = requireName(name);
        this.updatedAt = TimeUtils.now();
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        return name.trim();
    }

    private static String requireKey(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            throw new IllegalArgumentException("imageKey is required");
        }
        return imageKey;
    }

    @Override
    public VisualBaselineId getId() {
        return id;
    }

    public ProjectId getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public String getImageKey() {
        return imageKey;
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
