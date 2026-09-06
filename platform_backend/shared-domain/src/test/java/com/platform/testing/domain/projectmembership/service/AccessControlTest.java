package com.platform.testing.domain.projectmembership.service;

import com.platform.testing.domain.project.valueobject.ProjectId;
import com.platform.testing.domain.projectmembership.ProjectMembership;
import com.platform.testing.domain.projectmembership.valueobject.Action;
import com.platform.testing.domain.projectmembership.valueobject.ResourceArn;
import com.platform.testing.domain.projectmembership.valueobject.Role;
import com.platform.testing.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessControlTest {

    @Test
    void emptyMembershipsDeny() {
        ProjectId projectId = ProjectId.of("proj-1");
        assertFalse(AccessControl.allows(
                List.of(),
                Action.CATALOG_READ,
                ResourceArn.of(projectId, "testcase", "c1")
        ));
    }

    @Test
    void rejectsNullArguments() {
        ProjectId projectId = ProjectId.of("proj-1");
        ProjectMembership viewer = ProjectMembership.create(UserId.generate(), projectId, Role.VIEWER);
        ResourceArn resource = ResourceArn.of(projectId, "testcase", "c1");

        assertThrows(NullPointerException.class, () -> AccessControl.allows(null, Action.CATALOG_READ, resource));
        assertThrows(NullPointerException.class, () -> AccessControl.allows(List.of(viewer), null, resource));
        assertThrows(NullPointerException.class, () -> AccessControl.allows(List.of(viewer), Action.CATALOG_READ, null));
    }

    @Test
    void viewerCanReadButNotWrite() {
        ProjectId projectId = ProjectId.of("proj-1");
        ProjectMembership viewer = ProjectMembership.create(UserId.generate(), projectId, Role.VIEWER);
        ResourceArn resource = ResourceArn.of(projectId, "testcase", "c1");

        assertTrue(AccessControl.allows(List.of(viewer), Action.CATALOG_READ, resource));
        assertFalse(AccessControl.allows(List.of(viewer), Action.CATALOG_WRITE, resource));
    }

    @Test
    void unionsAllowsAcrossMemberships() {
        ProjectId first = ProjectId.of("proj-1");
        ProjectId second = ProjectId.of("proj-2");
        ProjectMembership viewer = ProjectMembership.create(UserId.generate(), first, Role.VIEWER);
        ProjectMembership editor = ProjectMembership.create(UserId.generate(), second, Role.EDITOR);

        assertTrue(AccessControl.allows(
                List.of(viewer, editor),
                Action.CATALOG_WRITE,
                ResourceArn.of(second, "testcase", "c1")
        ));
        assertFalse(AccessControl.allows(
                List.of(viewer, editor),
                Action.CATALOG_WRITE,
                ResourceArn.of(first, "testcase", "c1")
        ));
    }

    @Test
    void inactiveMembershipDoesNotGrant() {
        ProjectId projectId = ProjectId.of("proj-1");
        ProjectMembership owner = ProjectMembership.create(UserId.generate(), projectId, Role.OWNER);
        owner.deactivate();

        assertFalse(AccessControl.allows(
                List.of(owner),
                Action.PROJECT_GET,
                ResourceArn.project(projectId)
        ));
    }
}
