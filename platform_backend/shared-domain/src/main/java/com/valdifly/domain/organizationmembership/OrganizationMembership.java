package com.valdifly.domain.organizationmembership;

import com.valdifly.domain.common.AggregateRoot;
import com.valdifly.domain.organization.valueobject.OrganizationId;
import com.valdifly.domain.organizationmembership.valueobject.OrganizationMembershipId;
import com.valdifly.domain.project.Project;
import com.valdifly.domain.common.Action;
import com.valdifly.domain.common.PolicyStatement;
import com.valdifly.domain.common.ResourceArn;
import com.valdifly.domain.roletemplate.RoleTemplate;
import com.valdifly.domain.roletemplate.valueobject.RoleTemplateId;
import com.valdifly.domain.user.valueobject.UserId;
import com.valdifly.utils.TimeUtils;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class OrganizationMembership implements AggregateRoot<OrganizationMembershipId> {

    private final OrganizationMembershipId id;
    private final UserId userId;
    private final OrganizationId organizationId;
    private RoleTemplateId roleTemplateId;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static OrganizationMembership create(UserId userId, OrganizationId organizationId, RoleTemplateId roleTemplateId) {
        return new OrganizationMembership(OrganizationMembershipId.generate(), userId, organizationId, roleTemplateId);
    }

    public static OrganizationMembership reconstitute(
            OrganizationMembershipId id,
            UserId userId,
            OrganizationId organizationId,
            RoleTemplateId roleTemplateId,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        OrganizationMembership membership = new OrganizationMembership(id, userId, organizationId, roleTemplateId);
        membership.active = active;
        membership.createdAt = createdAt;
        membership.updatedAt = updatedAt;
        return membership;
    }

    private OrganizationMembership(
            OrganizationMembershipId id,
            UserId userId,
            OrganizationId organizationId,
            RoleTemplateId roleTemplateId
    ) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.roleTemplateId = Objects.requireNonNull(roleTemplateId, "roleTemplateId is required");
        this.active = true;
        this.createdAt = TimeUtils.now();
        this.updatedAt = this.createdAt;
    }

    public void assignTemplate(RoleTemplateId roleTemplateId) {
        this.roleTemplateId = Objects.requireNonNull(roleTemplateId, "roleTemplateId is required");
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

    /**
     * Statements from the assigned template; a missing template or one owned by a different
     * scope yields no statements (fail closed — evaluation denies, it does not throw).
     */
    public List<PolicyStatement> policy(RoleTemplate template) {
        if (template == null || !template.isOwnedBy(organizationId)) {
            return List.of();
        }
        return template.policy();
    }

    public boolean allows(Action action, ResourceArn resource, RoleTemplate template) {
        if (!active) {
            return false;
        }
        List<PolicyStatement> statements = policy(template);
        if (statements.stream().anyMatch(statement -> statement.denies(action, resource))) {
            return false;
        }
        return statements.stream().anyMatch(statement -> statement.permits(action, resource));
    }

    /**
     * Org policy covers child projects: the assigned template's actions apply to every project
     * in this organization (org-scope statements never cover plain project ARNs, hence this path).
     * DENY statements are honored symmetrically with the statement path, so a future DENY in a
     * template excludes child projects too (dead machinery today — Stage 1 is ALLOW-only).
     */
    public boolean grants(Action action, Project project, RoleTemplate template) {
        Objects.requireNonNull(action);
        Objects.requireNonNull(project);
        if (!active || project.getOrganizationId() == null || template == null) {
            return false;
        }
        ResourceArn projectArn = ResourceArn.project(project.getId());
        return organizationId.equals(project.getOrganizationId())
                && template.isOwnedBy(organizationId)
                && template.policy().stream().noneMatch(statement -> statement.denies(action, projectArn))
                && template.permits(action);
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

    public RoleTemplateId getRoleTemplateId() {
        return roleTemplateId;
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
