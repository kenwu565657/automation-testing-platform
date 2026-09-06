package com.platform.testing.domain.projectmembership;

import com.platform.testing.domain.common.AggregateRoot;
import com.platform.testing.domain.projectmembership.valueobject.Action;
import com.platform.testing.domain.projectmembership.valueobject.PolicyStatement;
import com.platform.testing.domain.projectmembership.valueobject.ProjectMembershipId;
import com.platform.testing.domain.projectmembership.valueobject.ResourceArn;
import com.platform.testing.domain.projectmembership.valueobject.Role;
import com.platform.testing.domain.user.valueobject.UserId;
import com.platform.testing.domain.project.valueobject.ProjectId;
import com.platform.testing.utils.TimeUtils;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class ProjectMembership implements AggregateRoot<ProjectMembershipId> {

    private final ProjectMembershipId id;
    private final UserId userId;
    private final ProjectId projectId;
    private Role role;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProjectMembership create(UserId userId, ProjectId projectId, Role role) {
        return new ProjectMembership(ProjectMembershipId.generate(), userId, projectId, role);
    }

    public static ProjectMembership reconstitute(
            ProjectMembershipId id,
            UserId userId,
            ProjectId projectId,
            Role role,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        ProjectMembership membership = new ProjectMembership(id, userId, projectId, role);
        membership.active = active;
        membership.createdAt = createdAt;
        membership.updatedAt = updatedAt;
        return membership;
    }

    private ProjectMembership(ProjectMembershipId id, UserId userId, ProjectId projectId, Role role) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.projectId = Objects.requireNonNull(projectId, "projectId is required");
        this.role = Objects.requireNonNull(role, "role is required");
        this.active = true;
        this.createdAt = TimeUtils.now();
        this.updatedAt = this.createdAt;
    }

    public void changeRole(Role role) {
        this.role = Objects.requireNonNull(role, "role is required");
        touch();
    }

    public void deactivate() {
        this.active = false;
        touch();
    }

    public List<PolicyStatement> policy() {
        return role.policy(projectId);
    }

    public boolean allows(Action action, ResourceArn resource) {
        if (!active) {
            return false;
        }
        if (policy().stream().anyMatch(statement -> statement.denies(action, resource))) {
            return false;
        }
        return policy().stream().anyMatch(statement -> statement.permits(action, resource));
    }

    public ResourceArn resourceArn() {
        return ResourceArn.project(projectId);
    }

    private void touch() {
        this.updatedAt = TimeUtils.now();
    }

    @Override
    public ProjectMembershipId getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public ProjectId getProjectId() {
        return projectId;
    }

    public Role getRole() {
        return role;
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
}
