package com.platform.testing.domain.execution.event;

import com.platform.testing.domain.common.DomainEvent;
import com.platform.testing.domain.common.EventType;
import com.platform.testing.domain.common.Priority;
import com.platform.testing.domain.environment.valueobject.EnvironmentId;
import com.platform.testing.domain.execution.valueobject.TestRunId;
import com.platform.testing.domain.target.valueobject.ExecutionTargetId;
import com.platform.testing.domain.testcase.valueobject.TestCaseId;
import com.platform.testing.domain.testcase.valueobject.TestType;
import com.platform.testing.domain.testsuite.valueobject.TestSuiteId;
import com.platform.testing.domain.user.valueobject.UserId;
import com.platform.testing.utils.TimeUtils;

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
