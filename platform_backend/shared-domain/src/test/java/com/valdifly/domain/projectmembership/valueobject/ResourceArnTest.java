package com.valdifly.domain.projectmembership.valueobject;

import com.valdifly.domain.organization.valueobject.OrganizationId;
import com.valdifly.domain.project.valueobject.ProjectId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.valdifly.domain.user.valueobject.UserId;

class ResourceArnTest {

    @Test
    void projectArnCoversChildren() {
        ProjectId projectId = ProjectId.of("proj-1");
        ResourceArn project = ResourceArn.project(projectId);
        ResourceArn testCase = ResourceArn.of(projectId, "testcase", "case-1");

        assertEquals("arn:valdifly:project:proj-1", project.value());
        assertEquals("arn:valdifly:project:proj-1:testcase:case-1", testCase.value());
        assertTrue(project.covers(testCase));
        assertTrue(project.covers(project));
        assertFalse(testCase.covers(project));
        assertEquals(projectId, project.projectId().orElseThrow());
        assertEquals(projectId, testCase.projectId().orElseThrow());
    }

    @Test
    void userArnIsNotAProject() {
        ResourceArn user = ResourceArn.user(UserId.of("u1"));
        assertEquals("arn:valdifly:user:u1", user.value());
        assertTrue(user.projectId().isEmpty());
        assertFalse(user.covers(ResourceArn.project(ProjectId.of("proj-1"))));
    }

    @Test
    void organizationArnCoversNestedProjectForm() {
        OrganizationId organizationId = OrganizationId.of("org-1");
        ResourceArn organization = ResourceArn.organization(organizationId);
        ResourceArn nested = new ResourceArn("arn:valdifly:organization:org-1:project:proj-1");

        assertEquals("arn:valdifly:organization:org-1", organization.value());
        assertTrue(organization.covers(nested));
        assertTrue(organization.covers(organization));
        assertEquals(organizationId, organization.organizationId().orElseThrow());
        assertEquals(organizationId, nested.organizationId().orElseThrow());
        assertEquals(ProjectId.of("proj-1"), nested.projectId().orElseThrow());
        assertTrue(organization.projectId().isEmpty());
        assertFalse(organization.covers(ResourceArn.project(ProjectId.of("proj-1"))));
    }

    @Test
    void rejectsNonArn() {
        assertThrows(IllegalArgumentException.class, () -> new ResourceArn("project:1"));
    }
}
