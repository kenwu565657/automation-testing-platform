package com.platform.testing.domain.execution.valueobject;

import com.platform.testing.domain.testdefinition.valueobject.TestStepId;

public record TestStepResult(
        TestStepId testStepId,
        int orderIndex,
        RunStatus status,
        long durationMs,
        String actualValue,
        String expectedValue,
        String errorMessage,
        String screenshotPath
) {
    public static TestStepResult passed(TestStepId testStepId, int orderIndex, long durationMs, String actualValue, String expectedValue) {
        return new TestStepResult(testStepId, orderIndex, RunStatus.PASSED, durationMs, actualValue, expectedValue, null, null);
    }

    public static TestStepResult failed(TestStepId testStepId, int orderIndex, long durationMs, String actualValue, String expectedValue, String error, String screenshot) {
        return new TestStepResult(testStepId, orderIndex, RunStatus.FAILED, durationMs, actualValue, expectedValue, error, screenshot);
    }

    public static TestStepResult skipped(TestStepId testStepId, int orderIndex) {
        return new TestStepResult(testStepId, orderIndex, RunStatus.SKIPPED, 0, null, null, null, null);
    }
}
