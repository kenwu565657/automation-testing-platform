package com.valdifly.domain.organizationmembership;

import com.valdifly.domain.organization.valueobject.OrganizationId;
import com.valdifly.domain.project.Project;
import com.valdifly.domain.project.valueobject.ProjectId;
import com.valdifly.domain.common.Action;
import com.valdifly.domain.common.ResourceArn;
import com.valdifly.domain.roletemplate.BuiltinTemplates;
import com.valdifly.domain.roletemplate.RoleTemplate;
import com.valdifly.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrganizationMembershipTest {

    @Test
    void orgEditorCanReadOrgAndGrantChildProjects() {
        OrganizationId organizationId = OrganizationId.of("org-1");
        RoleTemplate editor = RoleTemplate.create(organizationId, "EDITOR", BuiltinTemplates.actionsOf(BuiltinTemplates.EDITOR));
        OrganizationMembership membership = OrganizationMembership.create(UserId.of("qa"), organizationId, editor.getId());
        Project child = Project.create("Shop", "desc", organizationId);

        assertTrue(membership.allows(Action.CATALOG_WRITE, ResourceArn.organization(organizationId), editor));
        assertFalse(membership.allows(Action.ORGANIZATION_DELETE, ResourceArn.organization(organizationId), editor));
        assertTrue(membership.grants(Action.PROJECT_GET, child, editor));
        assertTrue(membership.grants(Action.CATALOG_WRITE, child, editor));
        assertFalse(membership.grants(Action.PROJECT_DELETE, child, editor));
        assertFalse(membership.grants(Action.PROJECT_GET, Project.create("Other", null, OrganizationId.of("org-2")), editor));
        assertFalse(membership.grants(Action.PROJECT_GET, Project.create("Legacy", null), editor));
    }

    @Test
    void inactiveMembershipDeniesOrgAndProjects() {
        OrganizationId organizationId = OrganizationId.of("org-1");
        RoleTemplate owner = RoleTemplate.create(organizationId, "OWNER", BuiltinTemplates.actionsOf(BuiltinTemplates.OWNER));
        OrganizationMembership membership = OrganizationMembership.create(UserId.of("qa"), organizationId, owner.getId());
        membership.deactivate();
        Project child = Project.create("Shop", null, organizationId);

        assertFalse(membership.allows(Action.ORGANIZATION_DELETE, ResourceArn.organization(organizationId), owner));
        assertFalse(membership.grants(Action.PROJECT_GET, child, owner));
    }
}
