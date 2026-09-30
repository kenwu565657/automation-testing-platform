package com.valdifly.domain.execution.event;

import com.valdifly.domain.common.DomainEvent;
import com.valdifly.domain.common.EventType;
import com.valdifly.domain.execution.valueobject.RunStatus;
import com.valdifly.domain.execution.valueobject.TestRunId;
import com.valdifly.domain.target.valueobject.ExecutionTargetId;
import com.valdifly.domain.testsuite.valueobject.TestSuiteId;
import com.valdifly.utils.TimeUtils;

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
