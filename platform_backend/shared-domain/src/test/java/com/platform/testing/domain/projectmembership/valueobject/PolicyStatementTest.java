package com.platform.testing.domain.projectmembership.valueobject;

import com.platform.testing.domain.project.valueobject.ProjectId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyStatementTest {

    @Test
    void allowPermitsCoveredResource() {
        ProjectId projectId = ProjectId.of("proj-1");
        PolicyStatement statement = PolicyStatement.allow(
                Set.of(Action.CATALOG_READ),
                ResourceArn.project(projectId)
        );

        assertTrue(statement.permits(Action.CATALOG_READ, ResourceArn.of(projectId, "testcase", "c1")));
        assertFalse(statement.permits(Action.CATALOG_WRITE, ResourceArn.of(projectId, "testcase", "c1")));
        assertFalse(statement.denies(Action.CATALOG_READ, ResourceArn.of(projectId, "testcase", "c1")));
    }

    @Test
    void denyBlocksCoveredResource() {
        ProjectId projectId = ProjectId.of("proj-1");
        PolicyStatement statement = PolicyStatement.deny(
                Set.of(Action.CATALOG_WRITE),
                ResourceArn.project(projectId)
        );

        assertTrue(statement.denies(Action.CATALOG_WRITE, ResourceArn.of(projectId, "testcase", "c1")));
        assertFalse(statement.denies(Action.CATALOG_READ, ResourceArn.of(projectId, "testcase", "c1")));
        assertFalse(statement.permits(Action.CATALOG_WRITE, ResourceArn.of(projectId, "testcase", "c1")));
    }

    @Test
    void rejectsEmptyActions() {
        assertThrows(IllegalArgumentException.class, () -> PolicyStatement.allow(
                Set.of(),
                ResourceArn.project(ProjectId.of("proj-1"))
        ));
    }
}
