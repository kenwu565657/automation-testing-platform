package com.valdifly.domain.execution.event;

import com.valdifly.domain.common.EventType;
import com.valdifly.domain.execution.valueobject.TestRunId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestRunCancelledEventTest {

    @Test
    void namesTheCancelledEvent() {
        Instant when = Instant.parse("2026-09-19T00:00:00Z");
        TestRunCancelledEvent event = new TestRunCancelledEvent(TestRunId.of("run-1"), when);

        assertEquals(EventType.TEST_RUN_CANCELLED, event.eventType());
        assertEquals(when, event.occurredAt());
        assertEquals("run-1", event.runId().value());
    }
}
