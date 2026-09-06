package com.platform.testing.domain.testcase.event;

import com.platform.testing.domain.common.DomainEvent;
import com.platform.testing.domain.common.EventType;
import com.platform.testing.domain.common.Priority;
import com.platform.testing.domain.project.valueobject.ProjectId;
import com.platform.testing.domain.testcase.valueobject.AuthoringStyle;
import com.platform.testing.domain.testcase.valueobject.TestCaseId;
import com.platform.testing.domain.testcase.valueobject.TestType;
import com.platform.testing.domain.user.valueobject.UserId;
import com.platform.testing.utils.TimeUtils;

import java.time.Instant;
import java.util.Set;

/**
 * A new test case was created.
 */
public record TestCaseCreatedEvent(
        TestCaseId testCaseId,
        String testCaseName,
        ProjectId projectId,
        TestType testType,
        AuthoringStyle authoringStyle,
        Priority priority,
        Set<String> tags,
        UserId createdBy,
        Instant createdAt
) implements DomainEvent {
    public TestCaseCreatedEvent {
        if (tags == null) {
            tags = Set.of();
        }

        if (createdAt == null) {
            createdAt = TimeUtils.now();
        }
    }

    @Override
    public String eventType() {
        return EventType.TEST_CASE_CREATED;
    }

    @Override
    public Instant occurredAt() {
        return createdAt;
    }
}
