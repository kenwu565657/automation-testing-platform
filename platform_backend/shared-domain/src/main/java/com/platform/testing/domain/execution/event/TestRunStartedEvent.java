package com.platform.testing.domain.execution.event;

import com.platform.testing.domain.common.DomainEvent;
import com.platform.testing.domain.common.EventType;
import com.platform.testing.domain.environment.valueobject.EnvironmentId;
import com.platform.testing.domain.execution.valueobject.TestRunId;
import com.platform.testing.domain.target.valueobject.ExecutionTargetId;
import com.platform.testing.domain.testsuite.valueobject.TestSuiteId;
import com.platform.testing.utils.TimeUtils;

import java.time.Instant;

/**
 * Engine reports that this test run has started.
 */
public record TestRunStartedEvent(
        TestRunId runId,
        TestSuiteId testSuiteId,
        ExecutionTargetId executionTargetId,
        EnvironmentId environmentId,
        int totalCases,
        Instant startedAt
) implements DomainEvent {
    public TestRunStartedEvent {
        if (startedAt == null) startedAt = TimeUtils.now();
    }

    @Override
    public String eventType() {
        return EventType.TEST_RUN_STARTED;
    }

    @Override
    public Instant occurredAt() {
        return startedAt;
    }
}
