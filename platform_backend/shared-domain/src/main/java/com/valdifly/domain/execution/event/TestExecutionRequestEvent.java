package com.valdifly.domain.execution.event;

import com.valdifly.domain.common.DomainEvent;
import com.valdifly.domain.common.EventType;
import com.valdifly.domain.common.Priority;
import com.valdifly.domain.environment.valueobject.EnvironmentId;
import com.valdifly.domain.execution.valueobject.TestRunId;
import com.valdifly.domain.target.valueobject.ExecutionTargetId;
import com.valdifly.domain.testcase.valueobject.TestCaseId;
import com.valdifly.domain.testcase.valueobject.TestType;
import com.valdifly.domain.testsuite.valueobject.TestSuiteId;
import com.valdifly.domain.user.valueobject.UserId;
import com.valdifly.utils.TimeUtils;

import java.time.Instant;

/**
 * Admin asks engine to run this case on this execution target.
 */
public record TestExecutionRequestEvent(
        TestRunId runId,
        TestCaseId testCaseId,
        TestSuiteId testSuiteId,
        EnvironmentId environmentId,
        ExecutionTargetId executionTargetId,
        UserId triggeredBy,
        TestType testType,
        Priority executionPriority,
        int timeoutSeconds,
        int retryCount,
        int definitionVersion,
        Instant requestedAt
) implements DomainEvent {
    public TestExecutionRequestEvent {
        if (requestedAt == null) {
            requestedAt = TimeUtils.now();
        }
    }

    @Override
    public String eventType() {
        return EventType.TEST_EXECUTION_REQUEST;
    }

    @Override
    public Instant occurredAt() {
        return requestedAt;
    }
}
