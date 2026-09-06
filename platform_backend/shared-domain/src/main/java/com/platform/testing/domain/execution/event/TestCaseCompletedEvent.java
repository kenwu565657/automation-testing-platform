package com.platform.testing.domain.execution.event;

import com.platform.testing.domain.common.DomainEvent;
import com.platform.testing.domain.common.EventType;
import com.platform.testing.domain.execution.valueobject.RunStatus;
import com.platform.testing.domain.execution.valueobject.TestRunId;
import com.platform.testing.domain.target.valueobject.ExecutionTargetId;
import com.platform.testing.domain.testcase.valueobject.TestCaseId;
import com.platform.testing.utils.TimeUtils;

import java.time.Instant;

/**
 * Engine reports that this test case has finished.
 */
public record TestCaseCompletedEvent(
        TestRunId runId,
        TestCaseId testCaseId,
        String testCaseName,
        ExecutionTargetId executionTargetId,
        RunStatus status,
        long durationMs,
        int totalSteps,
        int passedSteps,
        int failedSteps,
        String errorMessage,
        String stackTrace,
        Instant completedAt
) implements DomainEvent {
    public TestCaseCompletedEvent {
        if (completedAt == null) {
            completedAt = TimeUtils.now();
        }
    }

    @Override
    public String eventType() {
        return EventType.TEST_CASE_COMPLETED;
    }

    @Override
    public Instant occurredAt() {
        return completedAt;
    }
}
