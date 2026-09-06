package com.platform.testing.domain.execution.event;

import com.platform.testing.domain.common.DomainEvent;
import com.platform.testing.domain.common.EventType;
import com.platform.testing.domain.execution.valueobject.RunStatus;
import com.platform.testing.domain.execution.valueobject.TestRunId;
import com.platform.testing.domain.testcase.valueobject.TestCaseId;
import com.platform.testing.domain.testdefinition.valueobject.TestStepId;
import com.platform.testing.utils.TimeUtils;

import java.time.Instant;
import java.util.Map;

/**
 * Engine reports that this step has finished.
 */
public record TestStepCompletedEvent(
        TestRunId runId,
        TestCaseId testCaseId,
        TestStepId testStepId,
        String stepName,
        int stepIndex,
        RunStatus status,
        long durationMs,
        String actualValue,
        String expectedValue,
        String errorMessage,
        String screenshotPath,
        Map<String, String> metadata,
        Instant completedAt
) implements DomainEvent {
    public TestStepCompletedEvent {
        if (metadata == null) {
            metadata = Map.of();
        }
        if (completedAt == null) {
            completedAt = TimeUtils.now();
        }
    }

    @Override
    public String eventType() {
        return EventType.TEST_STEP_COMPLETED;
    }

    @Override
    public Instant occurredAt() {
        return completedAt;
    }
}
