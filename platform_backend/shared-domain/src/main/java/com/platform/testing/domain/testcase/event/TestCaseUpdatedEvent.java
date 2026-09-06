package com.platform.testing.domain.testcase.event;

import com.platform.testing.domain.common.DomainEvent;
import com.platform.testing.domain.common.EventType;
import com.platform.testing.domain.common.Priority;
import com.platform.testing.domain.project.valueobject.ProjectId;
import com.platform.testing.domain.testcase.valueobject.AuthoringStyle;
import com.platform.testing.domain.testcase.valueobject.TestCaseId;
import com.platform.testing.domain.testcase.valueobject.TestType;
import com.platform.testing.utils.TimeUtils;

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
