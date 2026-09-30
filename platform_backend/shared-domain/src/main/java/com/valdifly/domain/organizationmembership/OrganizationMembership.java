package com.valdifly.domain.organizationmembership;

import com.valdifly.domain.common.AggregateRoot;
import com.valdifly.domain.organization.valueobject.OrganizationId;
import com.valdifly.domain.organizationmembership.valueobject.OrganizationMembershipId;
import com.valdifly.domain.project.Project;
import com.valdifly.domain.projectmembership.valueobject.Action;
import com.valdifly.domain.projectmembership.valueobject.PolicyStatement;
import com.valdifly.domain.projectmembership.valueobject.ResourceArn;
import com.valdifly.domain.projectmembership.valueobject.Role;
import com.valdifly.domain.user.valueobject.UserId;
import com.valdifly.utils.TimeUtils;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class OrganizationMembership implements AggregateRoot<OrganizationMembershipId> {

    private final OrganizationMembershipId id;
    private final UserId userId;
    private final OrganizationId organizationId;
    private Role role;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static OrganizationMembership create(UserId userId, OrganizationId organizationId, Role role) {
        return new OrganizationMembership(OrganizationMembershipId.generate(), userId, organizationId, role);
    }

    public static OrganizationMembership reconstitute(
            OrganizationMembershipId id,
            UserId userId,
            OrganizationId organizationId,
            Role role,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        OrganizationMembership membership = new OrganizationMembership(id, userId, organizationId, role);
        membership.active = active;
        membership.createdAt = createdAt;
        membership.updatedAt = updatedAt;
        return membership;
    }

    private OrganizationMembership(
            OrganizationMembershipId id,
            UserId userId,
            OrganizationId organizationId,
            Role role
    ) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
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

    public void activate() {
        this.active = true;
        touch();
    }

    public List<PolicyStatement> policy() {
        return role.policy(organizationId);
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

    /**
     * Org policy covers child projects: same Role actions apply to every project in this organization.
     */
    public boolean grants(Action action, Project project) {
        Objects.requireNonNull(action);
        Objects.requireNonNull(project);
        if (!active || project.getOrganizationId() == null) {
            return false;
        }
        return organizationId.equals(project.getOrganizationId()) && role.actions().contains(action);
    }

    public ResourceArn resourceArn() {
        return ResourceArn.organization(organizationId);
    }

    private void touch() {
        this.updatedAt = TimeUtils.now();
    }

    @Override
    public OrganizationMembershipId getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public OrganizationId getOrganizationId() {
        return organizationId;
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
