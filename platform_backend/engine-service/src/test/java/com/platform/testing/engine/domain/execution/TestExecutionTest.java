package com.platform.testing.engine.domain.execution;

import com.platform.testing.domain.execution.StepExecutionResult;
import com.platform.testing.domain.execution.TestExecution;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestExecutionTest {

    @Test
    void newExecutionIsPending() {
        var exec = new TestExecution("tc-1", "Login Test", "WEB_E2E", "env-1");
        assertThat(exec.getStatus()).isEqualTo(ExecutionStatus.PENDING);
        assertThat(exec.getExecutionId()).isNotNull();
        assertThat(exec.getStepResults()).isEmpty();
    }

    @Test
    void startTransitionsToPending() {
        var exec = new TestExecution("tc-1", "Login Test", "WEB_E2E", "env-1");
        exec.start();
        assertThat(exec.getStatus()).isEqualTo(ExecutionStatus.RUNNING);
        assertThat(exec.getStartedAt()).isNotNull();
    }

    @Test
    void cannotStartTwice() {
        var exec = new TestExecution("tc-1", "Login Test", "WEB_E2E", "env-1");
        exec.start();
        assertThatThrownBy(exec::start).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void recordPassedStep() {
        var exec = new TestExecution("tc-1", "Login Test", "WEB_E2E", "env-1");
        exec.start();
        exec.recordStepResult(StepExecutionResult.passed(0, "click login", "WHEN", 150));

        assertThat(exec.getCurrentStepIndex()).isEqualTo(1);
        assertThat(exec.getStepResults()).hasSize(1);
        assertThat(exec.getStatus()).isEqualTo(ExecutionStatus.RUNNING);
    }

    @Test
    void recordFailedStepTransitionsToFailed() {
        var exec = new TestExecution("tc-1", "Login Test", "WEB_E2E", "env-1");
        exec.start();
        exec.recordStepResult(StepExecutionResult.failed(0, "click login", "WHEN", 200, "Element not found", null));

        assertThat(exec.getStatus()).isEqualTo(ExecutionStatus.FAILED);
        assertThat(exec.getFinishedAt()).isNotNull();
        assertThat(exec.getErrorMessage()).isEqualTo("Element not found");
    }

    @Test
    void completeAfterAllStepsPass() {
        var exec = new TestExecution("tc-1", "Login Test", "WEB_E2E", "env-1");
        exec.start();
        exec.recordStepResult(StepExecutionResult.passed(0, "step 1", "GIVEN", 100));
        exec.recordStepResult(StepExecutionResult.passed(1, "step 2", "WHEN", 200));
        exec.complete();

        assertThat(exec.getStatus()).isEqualTo(ExecutionStatus.PASSED);
        assertThat(exec.getFinishedAt()).isNotNull();
        assertThat(exec.durationMillis()).isGreaterThan(0);
    }

    @Test
    void abortSetsStatusAndReason() {
        var exec = new TestExecution("tc-1", "Login Test", "WEB_E2E", "env-1");
        exec.start();
        exec.abort("Timeout waiting for driver");

        assertThat(exec.getStatus()).isEqualTo(ExecutionStatus.ABORTED);
        assertThat(exec.getErrorMessage()).isEqualTo("Timeout waiting for driver");
    }

    @Test
    void progressPercent() {
        var exec = new TestExecution("tc-1", "Login Test", "WEB_E2E", "env-1");
        exec.start();
        assertThat(exec.progressPercent(5)).isEqualTo(0.0);
        exec.recordStepResult(StepExecutionResult.passed(0, "step", "GIVEN", 50));
        assertThat(exec.progressPercent(5)).isEqualTo(20.0);
    }

    @Test
    void toJsonContainsAllFields() {
        var exec = new TestExecution("tc-1", "Login Test", "WEB_E2E", "env-1");
        exec.start();
        exec.recordStepResult(StepExecutionResult.passed(0, "step 1", "GIVEN", 100));
        exec.complete();

        var json = exec.toJson();
        assertThat(json.getString("executionId")).isNotNull();
        assertThat(json.getString("status")).isEqualTo("PASSED");
        assertThat(json.getString("testCaseId")).isEqualTo("tc-1");
        assertThat(json.getJsonArray("stepResults")).hasSize(1);
        assertThat(json.getLong("durationMillis")).isGreaterThan(0);
    }
}
