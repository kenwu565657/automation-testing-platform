package com.platform.testing.domain.execution.entity;

import com.platform.testing.domain.testcase.valueobject.TestCaseId;
import com.platform.testing.domain.common.DomainEntity;
import com.platform.testing.domain.execution.valueobject.RunStatus;
import com.platform.testing.domain.execution.valueobject.TestCaseResultId;
import com.platform.testing.domain.execution.valueobject.TestStepResult;
import com.platform.testing.domain.target.valueobject.ExecutionTargetId;
import com.platform.testing.utils.TimeUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TestCaseResult implements DomainEntity<TestCaseResultId> {

    private final TestCaseResultId id;
    private final TestCaseId testCaseId;
    private final int definitionVersion;
    private final ExecutionTargetId executionTargetId;
    private RunStatus status;
    private final List<TestStepResult> stepResults;
    private Instant startedAt;
    private Instant completedAt;
    private long durationMs;
    private String errorMessage;

    public static TestCaseResult create(TestCaseId testCaseId, int definitionVersion,
                                        ExecutionTargetId executionTargetId) {
        return new TestCaseResult(TestCaseResultId.generate(), testCaseId, definitionVersion, executionTargetId);
    }

    public static TestCaseResult reconstitute(TestCaseResultId id, TestCaseId testCaseId, int definitionVersion,
                                              ExecutionTargetId executionTargetId, RunStatus status,
                                              List<TestStepResult> stepResults, Instant startedAt,
                                              Instant completedAt, long durationMs, String errorMessage) {
        TestCaseResult r = new TestCaseResult(id, testCaseId, definitionVersion, executionTargetId);
        r.status = status;
        r.stepResults.addAll(stepResults);
        r.startedAt = startedAt;
        r.completedAt = completedAt;
        r.durationMs = durationMs;
        r.errorMessage = errorMessage;
        return r;
    }

    private TestCaseResult(TestCaseResultId id, TestCaseId testCaseId, int definitionVersion,
                           ExecutionTargetId executionTargetId) {
        if (definitionVersion < 1) {
            throw new IllegalArgumentException("definitionVersion must be >= 1");
        }
        this.id = Objects.requireNonNull(id);
        this.testCaseId = testCaseId;
        this.definitionVersion = definitionVersion;
        this.executionTargetId = executionTargetId;
        this.status = RunStatus.QUEUED;
        this.stepResults = new ArrayList<>();
        this.startedAt = TimeUtils.now();
    }

    public void addStepResult(TestStepResult result) {
        this.stepResults.add(result);
    }

    public void complete(RunStatus status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
        this.completedAt = TimeUtils.now();
        this.durationMs = completedAt.toEpochMilli() - startedAt.toEpochMilli();
    }

    public TestCaseResultId getId() {
        return id;
    }

    public TestCaseId getTestCaseId() {
        return testCaseId;
    }

    public int getDefinitionVersion() {
        return definitionVersion;
    }

    public ExecutionTargetId getExecutionTargetId() {
        return executionTargetId;
    }

    public RunStatus getStatus() {
        return status;
    }

    public List<TestStepResult> getStepResults() {
        return Collections.unmodifiableList(stepResults);
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
