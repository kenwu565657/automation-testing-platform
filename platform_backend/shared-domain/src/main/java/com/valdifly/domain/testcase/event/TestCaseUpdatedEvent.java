package com.valdifly.domain.testcase.event;

import com.valdifly.domain.common.DomainEvent;
import com.valdifly.domain.common.EventType;
import com.valdifly.domain.common.Priority;
import com.valdifly.domain.project.valueobject.ProjectId;
import com.valdifly.domain.testcase.valueobject.AuthoringStyle;
import com.valdifly.domain.testcase.valueobject.TestCaseId;
import com.valdifly.domain.testcase.valueobject.TestType;
import com.valdifly.utils.TimeUtils;

import java.time.Instant;
import java.util.Set;

/**
 * An existing test case was modified.
 */
public record TestCaseUpdatedEvent(
        TestCaseId testCaseId,
        String testCaseName,
        ProjectId projectId,
        TestType testType,
        AuthoringStyle authoringStyle,
        Priority priority,
        Set<String> tags,
        int latestDefinitionVersion,
        boolean active,
        Instant updatedAt
) implements DomainEvent {
    public TestCaseUpdatedEvent {
        if (tags == null) {
            tags = Set.of();
        }

        if (updatedAt == null) {
            updatedAt = TimeUtils.now();
        }
    }

    @Override
    public String eventType() {
        return EventType.TEST_CASE_UPDATED;
    }

    @Override
    public Instant occurredAt() {
        return updatedAt;
    }
}
