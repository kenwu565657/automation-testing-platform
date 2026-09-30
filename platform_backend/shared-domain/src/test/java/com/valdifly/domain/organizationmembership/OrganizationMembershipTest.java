package com.valdifly.domain.organizationmembership;

import com.valdifly.domain.organization.valueobject.OrganizationId;
import com.valdifly.domain.project.Project;
import com.valdifly.domain.projectmembership.valueobject.Action;
import com.valdifly.domain.projectmembership.valueobject.ResourceArn;
import com.valdifly.domain.projectmembership.valueobject.Role;
import com.valdifly.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrganizationMembershipTest {

    @Test
    void orgEditorCanReadOrgAndGrantChildProjects() {
        OrganizationId organizationId = OrganizationId.of("org-1");
        OrganizationMembership membership = OrganizationMembership.create(
                UserId.of("qa"), organizationId, Role.EDITOR
        );
        Project child = Project.create("Shop", null, organizationId);
        Project other = Project.create("Other", null, OrganizationId.of("org-2"));

        assertTrue(membership.allows(Action.ORGANIZATION_GET, ResourceArn.organization(organizationId)));
        assertTrue(membership.allows(Action.ORGANIZATION_CREATE_PROJECT, ResourceArn.organization(organizationId)));
        assertFalse(membership.allows(Action.ORGANIZATION_DELETE, ResourceArn.organization(organizationId)));
        assertTrue(membership.grants(Action.PROJECT_GET, child));
        assertTrue(membership.grants(Action.CATALOG_WRITE, child));
        assertFalse(membership.grants(Action.PROJECT_DELETE, child));
        assertFalse(membership.grants(Action.PROJECT_GET, other));
        assertFalse(membership.grants(Action.PROJECT_GET, Project.create("Legacy", null)));
    }

    @Test
    void inactiveMembershipDeniesOrgAndProjects() {
        OrganizationId organizationId = OrganizationId.of("org-1");
        OrganizationMembership membership = OrganizationMembership.create(
                UserId.of("qa"), organizationId, Role.OWNER
        );
        Project child = Project.create("Shop", null, organizationId);
        membership.deactivate();

        assertFalse(membership.allows(Action.ORGANIZATION_GET, ResourceArn.organization(organizationId)));
        assertFalse(membership.grants(Action.PROJECT_GET, child));
    }
}
