package com.valdifly.domain.projectmembership.valueobject;

import com.valdifly.domain.organization.valueobject.OrganizationId;
import com.valdifly.domain.project.valueobject.ProjectId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleTest {

    @Test
    void viewerCannotWriteOrTrigger() {
        assertTrue(Role.VIEWER.actions().contains(Action.CATALOG_READ));
        assertTrue(Role.VIEWER.actions().contains(Action.REPORT_READ));
        assertTrue(Role.VIEWER.actions().contains(Action.SCHEDULE_READ));
        assertTrue(Role.VIEWER.actions().contains(Action.ORGANIZATION_GET));
        assertFalse(Role.VIEWER.actions().contains(Action.ORGANIZATION_CREATE_PROJECT));
        assertFalse(Role.VIEWER.actions().contains(Action.CATALOG_WRITE));
        assertFalse(Role.VIEWER.actions().contains(Action.RUN_TRIGGER));
        assertFalse(Role.VIEWER.actions().contains(Action.PROJECT_DELETE));
    }

    @Test
    void runnerCanTriggerButNotEditCatalog() {
        assertTrue(Role.RUNNER.actions().contains(Action.RUN_TRIGGER));
        assertTrue(Role.RUNNER.actions().contains(Action.CATALOG_READ));
        assertFalse(Role.RUNNER.actions().contains(Action.CATALOG_WRITE));
        assertFalse(Role.RUNNER.actions().contains(Action.PROJECT_MANAGE_MEMBERS));
    }

    @Test
    void editorCanWriteCatalogButNotManageMembers() {
        assertTrue(Role.EDITOR.actions().contains(Action.CATALOG_WRITE));
        assertTrue(Role.EDITOR.actions().contains(Action.SCHEDULE_WRITE));
        assertTrue(Role.EDITOR.actions().contains(Action.ORGANIZATION_CREATE_PROJECT));
        assertTrue(Role.EDITOR.actions().contains(Action.RUN_TRIGGER));
        assertFalse(Role.EDITOR.actions().contains(Action.ORGANIZATION_DELETE));
        assertFalse(Role.EDITOR.actions().contains(Action.PROJECT_MANAGE_MEMBERS));
        assertFalse(Role.EDITOR.actions().contains(Action.PROJECT_DELETE));
    }

    @Test
    void ownerPolicyCoversTheProjectResource() {
        ProjectId projectId = ProjectId.of("proj-1");
        PolicyStatement statement = Role.OWNER.policy(projectId).getFirst();
        assertTrue(statement.permits(Action.PROJECT_DELETE, ResourceArn.project(projectId)));
        assertTrue(statement.permits(
                Action.CATALOG_WRITE,
                ResourceArn.of(projectId, "testcase", "case-1")
        ));
        assertFalse(statement.permits(Action.PROJECT_DELETE, ResourceArn.project(ProjectId.of("other"))));
    }

    @Test
    void ownerOrgPolicyCoversTheOrganization() {
        OrganizationId organizationId = OrganizationId.of("org-1");
        PolicyStatement statement = Role.OWNER.policy(organizationId).getFirst();
        assertTrue(statement.permits(Action.ORGANIZATION_DELETE, ResourceArn.organization(organizationId)));
        assertTrue(statement.permits(Action.ORGANIZATION_CREATE_PROJECT, ResourceArn.organization(organizationId)));
        assertFalse(statement.permits(
                Action.ORGANIZATION_DELETE,
                ResourceArn.organization(OrganizationId.of("other"))
        ));
    }
}
