package com.platform.testing.domain.execution.valueobject;

import com.platform.testing.domain.testdefinition.valueobject.TestStepId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestStepResultTest {

    @Test
    void factories() {
        TestStepId stepId = TestStepId.of("s");
        assertEquals(RunStatus.PASSED, TestStepResult.passed(stepId, 1, 5, "a", "a").status());
        assertEquals("err", TestStepResult.failed(stepId, 2, 8, "a", "b", "err", "/shot.png").errorMessage());
        TestStepResult skipped = TestStepResult.skipped(stepId, 3);
        assertEquals(RunStatus.SKIPPED, skipped.status());
        assertEquals(0, skipped.durationMs());
        assertNull(skipped.errorMessage());
    }
}
