package com.platform.testing.domain.common;

import com.platform.testing.domain.environment.valueobject.EnvironmentId;
import com.platform.testing.domain.execution.event.TestExecutionRequestEvent;
import com.platform.testing.domain.execution.valueobject.TestRunId;
import com.platform.testing.domain.target.valueobject.ExecutionTargetId;
import com.platform.testing.domain.testcase.valueobject.TestCaseId;
import com.platform.testing.domain.testcase.valueobject.TestType;
import com.platform.testing.domain.testsuite.valueobject.TestSuiteId;
import com.platform.testing.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainEventEnvelopeTest {

    @Test
    void wrapSetsMetadata() {
        TestExecutionRequestEvent payload = new TestExecutionRequestEvent(
                TestRunId.of("run-1"), TestCaseId.of("case-1"), TestSuiteId.of("suite-1"),
                EnvironmentId.of("env-1"), ExecutionTargetId.of("target-1"), UserId.of("qa"),
                TestType.API, Priority.HIGH, 30, 1, 1, Instant.parse("2026-01-01T00:00:00Z")
        );
        DomainEventEnvelope<TestExecutionRequestEvent> envelope =
                DomainEventEnvelope.wrap(payload, "admin-service", "run-1");

        assertEquals(EventType.TEST_EXECUTION_REQUEST, envelope.metadata().eventType());
        assertEquals("admin-service", envelope.metadata().sourceService());
        assertEquals("run-1", envelope.metadata().correlationId());
        assertEquals(payload, envelope.payload());
    }

    @Test
    void rejectsNullParts() {
        assertThrows(NullPointerException.class, () -> new DomainEventEnvelope<>(null, "x"));
    }
}
