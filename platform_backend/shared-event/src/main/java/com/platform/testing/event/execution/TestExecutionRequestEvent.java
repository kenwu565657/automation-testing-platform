package com.platform.testing.event.execution;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.platform.testing.domain.constant.Priority;
import com.platform.testing.domain.constant.TestType;

import java.time.Instant;

/**
 * Admin → Engine (via Kafka).
 * "Please execute this test case on this execution target."
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TestExecutionRequestEvent(
        String runId,
        String testCaseId,
        String testSuiteId,
        String environmentId,
        String executionTargetId,
        String triggeredBy,
        TestType testType,
        Priority executionPriority,
        int timeoutSeconds,
        int retryCount,
        Instant requestedAt
) {
    public TestExecutionRequestEvent {
        if (requestedAt == null) {
            requestedAt = Instant.now();
        }
    }
}
