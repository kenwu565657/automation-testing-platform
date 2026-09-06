package com.platform.testing.domain.execution;

import com.platform.testing.domain.testsuite.valueobject.TestSuiteId;
import com.platform.testing.domain.common.AggregateRoot;
import com.platform.testing.domain.environment.valueobject.EnvironmentId;
import com.platform.testing.domain.execution.entity.TestCaseResult;
import com.platform.testing.domain.execution.valueobject.RunStatus;
import com.platform.testing.domain.execution.valueobject.TestRunId;
import com.platform.testing.domain.execution.valueobject.TriggerKind;
import com.platform.testing.domain.target.valueobject.ExecutionTargetId;
import com.platform.testing.domain.user.valueobject.UserId;
import com.platform.testing.utils.TimeUtils;

import java.time.Instant;
import java.util.*;

public class TestRun implements AggregateRoot<TestRunId> {

    private final TestRunId id;
    private final TestSuiteId testSuiteId;
    private final ExecutionTargetId executionTargetId;
    private final EnvironmentId environmentId;
    private RunStatus status;
    private final TriggerKind triggerKind;
    private final UserId triggeredBy;
    private Instant startedAt;
    private Instant completedAt;
    private long durationMs;
    private final List<TestCaseResult> caseResults;

    public static TestRun create(TestSuiteId testSuiteId, ExecutionTargetId executionTargetId,
                                 EnvironmentId environmentId, TriggerKind triggerKind, UserId triggeredBy) {
        return new TestRun(TestRunId.generate(), testSuiteId, executionTargetId,
                environmentId, triggerKind, triggeredBy);
    }

    public static TestRun reconstitute(TestRunId id, TestSuiteId testSuiteId,
                                       ExecutionTargetId executionTargetId, EnvironmentId environmentId,
                                       RunStatus status, TriggerKind triggerKind, UserId triggeredBy,
                                       Instant startedAt, Instant completedAt, long durationMs,
                                       List<TestCaseResult> caseResults) {
        TestRun tr = new TestRun(id, testSuiteId, executionTargetId, environmentId, triggerKind, triggeredBy);
        tr.status = status;
        tr.startedAt = startedAt;
        tr.completedAt = completedAt;
        tr.durationMs = durationMs;
        tr.caseResults.addAll(caseResults);
        return tr;
    }

    private TestRun(TestRunId id, TestSuiteId testSuiteId, ExecutionTargetId executionTargetId,
                    EnvironmentId environmentId, TriggerKind triggerKind, UserId triggeredBy) {
        this.id = Objects.requireNonNull(id);
        this.testSuiteId = Objects.requireNonNull(testSuiteId);
        this.executionTargetId = Objects.requireNonNull(executionTargetId);
        this.environmentId = Objects.requireNonNull(environmentId);
        this.triggerKind = triggerKind != null ? triggerKind : TriggerKind.MANUAL;
        this.triggeredBy = Objects.requireNonNull(triggeredBy, "triggeredBy is required");
        this.status = RunStatus.QUEUED;
        this.startedAt = TimeUtils.now();
        this.caseResults = new ArrayList<>();
    }

    public void markRunning() {
        this.status = RunStatus.RUNNING;
    }

    public void recordCaseResult(TestCaseResult result) {
        this.caseResults.add(result);
    }

    public void complete() {
        boolean anyFailed = caseResults.stream()
                .anyMatch(r -> r.getStatus() == RunStatus.FAILED || r.getStatus() == RunStatus.ERROR);
        this.status = anyFailed ? RunStatus.FAILED : RunStatus.PASSED;
        this.completedAt = TimeUtils.now();
        this.durationMs = completedAt.toEpochMilli() - startedAt.toEpochMilli();
    }

    public void cancel() {
        this.status = RunStatus.CANCELLED;
        this.completedAt = TimeUtils.now();
    }

    // ── Query ──
    public int totalCases() { return caseResults.size(); }
    public long passedCases() { return caseResults.stream().filter(r -> r.getStatus() == RunStatus.PASSED).count(); }
    public long failedCases() { return caseResults.stream().filter(r -> r.getStatus() == RunStatus.FAILED).count(); }

    // ── Getters ──
    public TestRunId getId() { return id; }
    public TestSuiteId getTestSuiteId() { return testSuiteId; }
    public ExecutionTargetId getExecutionTargetId() { return executionTargetId; }
    public EnvironmentId getEnvironmentId() { return environmentId; }
    public RunStatus getStatus() { return status; }
    public TriggerKind getTriggerKind() { return triggerKind; }
    public UserId getTriggeredBy() { return triggeredBy; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public long getDurationMs() { return durationMs; }
    public List<TestCaseResult> getCaseResults() { return Collections.unmodifiableList(caseResults); }
}
