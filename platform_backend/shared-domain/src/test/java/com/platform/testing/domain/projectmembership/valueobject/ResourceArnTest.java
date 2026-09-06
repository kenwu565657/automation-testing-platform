package com.platform.testing.domain.projectmembership.valueobject;

import com.platform.testing.domain.project.valueobject.ProjectId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.platform.testing.domain.user.valueobject.UserId;

class ResourceArnTest {

    @Test
    void projectArnCoversChildren() {
        ProjectId projectId = ProjectId.of("proj-1");
        ResourceArn project = ResourceArn.project(projectId);
        ResourceArn testCase = ResourceArn.of(projectId, "testcase", "case-1");

        assertEquals("arn:testops:project:proj-1", project.value());
        assertEquals("arn:testops:project:proj-1:testcase:case-1", testCase.value());
        assertTrue(project.covers(testCase));
        assertTrue(project.covers(project));
        assertFalse(testCase.covers(project));
        assertEquals(projectId, project.projectId().orElseThrow());
        assertEquals(projectId, testCase.projectId().orElseThrow());
    }

    @Test
    void userArnIsNotAProject() {
        ResourceArn user = ResourceArn.user(UserId.of("u1"));
        assertEquals("arn:testops:user:u1", user.value());
        assertTrue(user.projectId().isEmpty());
        assertFalse(user.covers(ResourceArn.project(ProjectId.of("proj-1"))));
    }

    @Test
    void rejectsNonArn() {
        assertThrows(IllegalArgumentException.class, () -> new ResourceArn("project:1"));
    }
}
