package com.platform.testing.domain.testsuite;

import com.platform.testing.domain.testcase.valueobject.TestCaseId;
import com.platform.testing.domain.project.valueobject.ProjectId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestSuiteTest {

    @Test
    void addAndRemoveCases() {
        TestSuite suite = TestSuite.create("Smoke", "nightly", ProjectId.generate());
        TestCaseId caseId = TestCaseId.generate();

        suite.addTestCase(caseId);
        assertEquals(List.of(caseId), suite.getTestCaseIds());
        assertThrows(IllegalArgumentException.class, () -> suite.addTestCase(caseId));

        suite.removeTestCase(caseId);
        assertTrue(suite.getTestCaseIds().isEmpty());
    }

    @Test
    void deactivateAndReconstitute() {
        TestSuite suite = TestSuite.create("Smoke", null, ProjectId.generate());
        suite.deactivate();
        assertFalse(suite.isActive());

        TestCaseId caseId = TestCaseId.generate();
        TestSuite restored = TestSuite.reconstitute(
                suite.getId(), "Smoke", "d", suite.getProjectId(),
                List.of(caseId), false,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z")
        );
        assertEquals(List.of(caseId), restored.getTestCaseIds());
        assertFalse(restored.isActive());
    }

    @Test
    void createRequiresNameAndProject() {
        assertThrows(NullPointerException.class, () -> TestSuite.create(null, "d", ProjectId.generate()));
        assertThrows(NullPointerException.class, () -> TestSuite.create("Smoke", "d", null));
    }
}
