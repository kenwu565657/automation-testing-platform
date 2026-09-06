package com.platform.testing.domain.execution.event;

import com.platform.testing.domain.common.DomainEvent;
import com.platform.testing.domain.common.EventType;
import com.platform.testing.domain.execution.valueobject.TestRunId;
import com.platform.testing.domain.target.valueobject.ExecutionTargetId;
import com.platform.testing.domain.testcase.valueobject.TestCaseId;
import com.platform.testing.utils.TimeUtils;

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
