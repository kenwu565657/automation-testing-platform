package com.valdifly.domain.execution.event;

import com.valdifly.domain.common.DomainEvent;
import com.valdifly.domain.common.EventType;
import com.valdifly.domain.execution.valueobject.TestRunId;
import com.valdifly.utils.TimeUtils;

import java.time.Instant;

public record TestRunCancelledEvent(
        TestRunId runId,
        Instant cancelledAt
) implements DomainEvent {
    public TestRunCancelledEvent {
        if (cancelledAt == null) {
            cancelledAt = TimeUtils.now();
        }
    }

    @Override
    public String eventType() {
        return EventType.TEST_RUN_CANCELLED;
    }

    @Override
    public Instant occurredAt() {
        return cancelledAt;
    }
}
