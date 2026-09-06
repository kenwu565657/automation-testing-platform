package com.platform.testing.domain.execution.event;

import com.platform.testing.domain.common.DomainEvent;
import com.platform.testing.domain.common.EventType;
import com.platform.testing.domain.execution.valueobject.RunStatus;
import com.platform.testing.domain.execution.valueobject.TestRunId;
import com.platform.testing.domain.target.valueobject.ExecutionTargetId;
import com.platform.testing.domain.testsuite.valueobject.TestSuiteId;
import com.platform.testing.utils.TimeUtils;

import java.time.Instant;

/**
 * Engine reports that this test run has finished.
 */
public record TestRunCompletedEvent(
        TestRunId runId,
        TestSuiteId testSuiteId,
        ExecutionTargetId executionTargetId,
        RunStatus status,
        long durationMs,
        int totalCases,
        int passedCases,
        int failedCases,
        int skippedCases,
        Instant completedAt
) implements DomainEvent {
    public TestRunCompletedEvent {
        if (completedAt == null) {
            completedAt = TimeUtils.now();
        }
    }

    @Override
    public String eventType() {
        return EventType.TEST_RUN_COMPLETED;
    }

    @Override
    public Instant occurredAt() {
        return completedAt;
    }
}
