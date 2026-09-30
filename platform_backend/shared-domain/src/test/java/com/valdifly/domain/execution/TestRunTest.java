package com.valdifly.domain.execution;

import com.valdifly.domain.testcase.valueobject.TestCaseId;
import com.valdifly.domain.testsuite.valueobject.TestSuiteId;
import com.valdifly.domain.environment.valueobject.EnvironmentId;
import com.valdifly.domain.execution.entity.TestCaseResult;
import com.valdifly.domain.execution.valueobject.RunStatus;
import com.valdifly.domain.execution.valueobject.TestCaseResultId;
import com.valdifly.domain.execution.valueobject.TestRunId;
import com.valdifly.domain.execution.valueobject.TriggerKind;
import com.valdifly.domain.target.valueobject.ExecutionTargetId;
import com.valdifly.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void cancelRejectsFinishedRun() {
        TestRun run = newRun();
        run.complete();
        assertThrows(IllegalStateException.class, run::cancel);
    }

    @Test
    void cancelRejectsAlreadyCancelled() {
        TestRun run = newRun();
        run.cancel();
        assertThrows(IllegalStateException.class, run::cancel);
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
