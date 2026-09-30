package com.valdifly.domain.execution.entity;

import com.valdifly.domain.testcase.valueobject.TestCaseId;
import com.valdifly.domain.execution.valueobject.RunStatus;
import com.valdifly.domain.execution.valueobject.TestStepResult;
import com.valdifly.domain.testdefinition.valueobject.TestStepId;
import com.valdifly.domain.target.valueobject.ExecutionTargetId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestCaseResultTest {

    @Test
    void completeRecordsStatusAndDuration() {
        TestCaseResult result = TestCaseResult.create(TestCaseId.generate(), 1, ExecutionTargetId.generate());
        assertEquals(RunStatus.QUEUED, result.getStatus());
        assertEquals(1, result.getDefinitionVersion());

        result.addStepResult(TestStepResult.passed(TestStepId.of("s1"), 1, 10, "ok", "ok"));
        result.complete(RunStatus.FAILED, "mismatch");

        assertEquals(RunStatus.FAILED, result.getStatus());
        assertEquals("mismatch", result.getErrorMessage());
        assertEquals(1, result.getStepResults().size());
        assertNotNull(result.getCompletedAt());
    }
}
