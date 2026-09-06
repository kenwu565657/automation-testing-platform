package com.platform.testing.domain.execution;

import com.platform.testing.domain.testcase.valueobject.TestCaseId;
import com.platform.testing.domain.testsuite.valueobject.TestSuiteId;
import com.platform.testing.domain.environment.valueobject.EnvironmentId;
import com.platform.testing.domain.execution.entity.TestCaseResult;
import com.platform.testing.domain.execution.valueobject.RunStatus;
import com.platform.testing.domain.execution.valueobject.TestCaseResultId;
import com.platform.testing.domain.execution.valueobject.TestRunId;
import com.platform.testing.domain.execution.valueobject.TriggerKind;
import com.platform.testing.domain.target.valueobject.ExecutionTargetId;
import com.platform.testing.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestRunTest {

    @Test
    void createStartsQueued() {
        TestRun run = newRun();
        assertEquals(RunStatus.QUEUED, run.getStatus());
        assertEquals(TriggerKind.MANUAL, run.getTriggerKind());
        assertEquals(0, run.totalCases());
        assertNotNull(run.getStartedAt());
    }

    @Test
    void completePassesWhenNoFailures() {
        TestRun run = newRun();
        TestCaseResult result = TestCaseResult.create(TestCaseId.generate(), 1, run.getExecutionTargetId());
        result.complete(RunStatus.PASSED, null);
        run.recordCaseResult(result);
        run.complete();

        assertEquals(RunStatus.PASSED, run.getStatus());
        assertEquals(1, run.passedCases());
        assertEquals(0, run.failedCases());
        assertNotNull(run.getCompletedAt());
    }

    @Test
    void completeFailsOnFailedOrErrorCase() {
        TestRun run = newRun();
        TestCaseResult failed = TestCaseResult.create(TestCaseId.generate(), 1, run.getExecutionTargetId());
        failed.complete(RunStatus.ERROR, "boom");
        run.recordCaseResult(failed);
        run.complete();

        assertEquals(RunStatus.FAILED, run.getStatus());
        assertEquals(0, run.failedCases());
    }

    @Test
    void cancelSetsCancelled() {
        TestRun run = newRun();
        run.cancel();
        assertEquals(RunStatus.CANCELLED, run.getStatus());
        assertNotNull(run.getCompletedAt());
    }

    @Test
    void reconstituteRestoresCounts() {
        TestRunId id = TestRunId.generate();
        TestCaseResult result = TestCaseResult.reconstitute(
                TestCaseResultId.of("r1"), TestCaseId.generate(), 2, ExecutionTargetId.generate(),
                RunStatus.PASSED, List.of(), Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:01Z"), 1000, null
        );
        TestRun run = TestRun.reconstitute(
                id, TestSuiteId.generate(), ExecutionTargetId.generate(), EnvironmentId.generate(),
                RunStatus.PASSED, TriggerKind.MANUAL, UserId.of("qa"), Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:02Z"), 2000, List.of(result)
        );
        assertEquals(1, run.totalCases());
        assertEquals(RunStatus.PASSED, run.getStatus());
    }

    private static TestRun newRun() {
        return TestRun.create(
                TestSuiteId.generate(),
                ExecutionTargetId.generate(),
                EnvironmentId.generate(),
                TriggerKind.MANUAL,
                UserId.of("qa")
        );
    }
}
