package com.platform.testing.domain.projectmembership;

import com.platform.testing.domain.projectmembership.valueobject.Action;
import com.platform.testing.domain.projectmembership.valueobject.ResourceArn;
import com.platform.testing.domain.projectmembership.valueobject.Role;
import com.platform.testing.domain.user.valueobject.UserId;
import com.platform.testing.domain.project.valueobject.ProjectId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectMembershipTest {

    @Test
    void membershipGrantsRoleOnProjectAndChildren() {
        ProjectId projectId = ProjectId.of("proj-1");
        ProjectMembership membership = ProjectMembership.create(UserId.generate(), projectId, Role.EDITOR);

        assertTrue(membership.allows(Action.CATALOG_WRITE, ResourceArn.of(projectId, "testcase", "c1")));
        assertTrue(membership.allows(Action.RUN_TRIGGER, ResourceArn.of(projectId, "run", "r1")));
        assertFalse(membership.allows(Action.PROJECT_DELETE, ResourceArn.project(projectId)));
        assertFalse(membership.allows(Action.CATALOG_WRITE, ResourceArn.project(ProjectId.of("other"))));
    }

    @Test
    void inactiveMembershipDeniesEverything() {
        ProjectId projectId = ProjectId.of("proj-1");
        ProjectMembership membership = ProjectMembership.create(UserId.generate(), projectId, Role.OWNER);
        membership.deactivate();

        assertFalse(membership.allows(Action.PROJECT_GET, ResourceArn.project(projectId)));
        assertFalse(membership.isActive());
    }

    @Test
    void changeRoleReplacesThePolicy() {
        ProjectId projectId = ProjectId.of("proj-1");
        ProjectMembership membership = ProjectMembership.create(UserId.generate(), projectId, Role.VIEWER);
        assertFalse(membership.allows(Action.RUN_TRIGGER, ResourceArn.project(projectId)));

        membership.changeRole(Role.RUNNER);
        assertEquals(Role.RUNNER, membership.getRole());
        assertTrue(membership.allows(Action.RUN_TRIGGER, ResourceArn.project(projectId)));
    }
}
