package com.valdifly.domain.execution.event;

import com.valdifly.domain.common.DomainEvent;
import com.valdifly.domain.common.EventType;
import com.valdifly.domain.environment.valueobject.EnvironmentId;
import com.valdifly.domain.execution.valueobject.TestRunId;
import com.valdifly.domain.target.valueobject.ExecutionTargetId;
import com.valdifly.domain.testsuite.valueobject.TestSuiteId;
import com.valdifly.utils.TimeUtils;

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
