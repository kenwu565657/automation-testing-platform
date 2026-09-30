package com.valdifly.domain.execution.event;

import com.valdifly.domain.common.DomainEvent;
import com.valdifly.domain.common.EventType;
import com.valdifly.domain.execution.valueobject.TestRunId;
import com.valdifly.domain.target.valueobject.ExecutionTargetId;
import com.valdifly.domain.testcase.valueobject.TestCaseId;
import com.valdifly.utils.TimeUtils;

import java.time.Instant;

/**
 * Engine reports that this test case has started.
 */
public record TestCaseStartedEvent(
        TestRunId runId,
        TestCaseId testCaseId,
        String testCaseName,
        ExecutionTargetId executionTargetId,
        int totalSteps,
        Instant startedAt
) implements DomainEvent {
    public TestCaseStartedEvent {
        if (startedAt == null) {
            startedAt = TimeUtils.now();
        }
    }

    @Override
    public String eventType() {
        return EventType.TEST_CASE_STARTED;
    }

    @Override
    public Instant occurredAt() {
        return startedAt;
    }
}
