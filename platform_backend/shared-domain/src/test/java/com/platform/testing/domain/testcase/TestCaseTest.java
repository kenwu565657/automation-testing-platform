package com.platform.testing.domain.testcase;

import com.platform.testing.domain.testcase.valueobject.AuthoringStyle;
import com.platform.testing.domain.common.Priority;
import com.platform.testing.domain.testcase.valueobject.TestCaseId;
import com.platform.testing.domain.testcase.valueobject.TestType;
import com.platform.testing.domain.project.valueobject.ProjectId;
import com.platform.testing.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestCaseTest {

    @Test
    void createDefaultsPolicyAndVersion() {
        TestCase testCase = newCase();

        assertEquals("Login", testCase.getName());
        assertEquals(TestType.WEB_E2E, testCase.getTestType());
        assertEquals(AuthoringStyle.SIMPLE, testCase.getAuthoringStyle());
        assertEquals(Priority.MEDIUM, testCase.getPriority());
        assertTrue(testCase.isActive());
        assertEquals(60, testCase.getTimeoutSeconds());
        assertEquals(0, testCase.getRetryCount());
        assertEquals(0, testCase.getLatestDefinitionVersion());
        assertFalse(testCase.hasPublishedDefinition());
    }

    @Test
    void tagsNormalizeAtPrefix() {
        TestCase testCase = newCase();
        testCase.addTag("smoke");
        testCase.addTag("@regression");

        assertEquals(Set.of("@smoke", "@regression"), testCase.getTags());
        testCase.removeTag("smoke");
        assertEquals(Set.of("@regression"), testCase.getTags());
    }

    @Test
    void catalogUpdatesDoNotPublishADefinition() {
        TestCase testCase = newCase();
        testCase.rename("Sign in");
        testCase.updateDescription("desc");
        testCase.changePriority(Priority.HIGH);
        testCase.setPolicy(30, 2);
        testCase.deactivate();

        assertEquals("Sign in", testCase.getName());
        assertEquals("desc", testCase.getDescription());
        assertEquals(Priority.HIGH, testCase.getPriority());
        assertEquals(30, testCase.getTimeoutSeconds());
        assertEquals(2, testCase.getRetryCount());
        assertFalse(testCase.isActive());
        assertEquals(0, testCase.getLatestDefinitionVersion());

        testCase.activate();
        assertTrue(testCase.isActive());
    }

    @Test
    void recordPublishedDefinitionRequiresSequentialVersions() {
        TestCase testCase = newCase();
        assertEquals(1, testCase.nextDefinitionVersion());

        testCase.recordPublishedDefinition(1);
        assertEquals(1, testCase.getLatestDefinitionVersion());
        assertEquals(2, testCase.nextDefinitionVersion());
        assertThrows(IllegalArgumentException.class, () -> testCase.recordPublishedDefinition(1));
        assertThrows(IllegalArgumentException.class, () -> testCase.recordPublishedDefinition(3));
    }

    @Test
    void reconstituteRestoresCatalog() {
        Instant created = Instant.parse("2026-01-01T00:00:00Z");
        Instant updated = Instant.parse("2026-01-02T00:00:00Z");
        TestCase restored = TestCase.reconstitute(
                TestCaseId.of("case-1"), ProjectId.of("proj-1"), "Login", "restored",
                TestType.API, AuthoringStyle.GHERKIN, Priority.LOW,
                Set.of("@smoke"), false, 30, 2, 4,
                created, updated, UserId.of("qa")
        );

        assertEquals("restored", restored.getDescription());
        assertEquals(AuthoringStyle.GHERKIN, restored.getAuthoringStyle());
        assertEquals(4, restored.getLatestDefinitionVersion());
        assertFalse(restored.isActive());
        assertEquals(created, restored.getCreatedAt());
    }

    @Test
    void createRejectsMissingIdentity() {
        assertThrows(NullPointerException.class, () ->
                TestCase.create(null, "Login", TestType.API, AuthoringStyle.SIMPLE, Priority.LOW, UserId.of("qa")));
        assertThrows(NullPointerException.class, () ->
                TestCase.create(ProjectId.generate(), null, TestType.API, AuthoringStyle.SIMPLE, Priority.LOW, UserId.of("qa")));
        assertThrows(IllegalArgumentException.class, () ->
                TestCase.create(ProjectId.generate(), "  ", TestType.API, AuthoringStyle.SIMPLE, Priority.LOW, UserId.of("qa")));
        assertThrows(NullPointerException.class, () ->
                TestCase.create(ProjectId.generate(), "Login", TestType.API, AuthoringStyle.SIMPLE, Priority.LOW, null));
        assertThrows(NullPointerException.class, () -> TestCase.reconstitute(
                TestCaseId.of("case-1"), ProjectId.of("proj-1"), "Login", null,
                TestType.API, AuthoringStyle.SIMPLE, Priority.LOW,
                Set.of(), true, 60, 0, 0,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"), null
        ));
    }

    static TestCase newCase() {
        return TestCase.create(
                ProjectId.generate(), "Login", TestType.WEB_E2E, AuthoringStyle.SIMPLE, null, UserId.of("qa")
        );
    }
}
