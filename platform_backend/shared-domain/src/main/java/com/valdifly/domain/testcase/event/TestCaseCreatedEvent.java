package com.valdifly.domain.testcase.event;

import com.valdifly.domain.common.DomainEvent;
import com.valdifly.domain.common.EventType;
import com.valdifly.domain.common.Priority;
import com.valdifly.domain.project.valueobject.ProjectId;
import com.valdifly.domain.testcase.valueobject.AuthoringStyle;
import com.valdifly.domain.testcase.valueobject.TestCaseId;
import com.valdifly.domain.testcase.valueobject.TestType;
import com.valdifly.domain.user.valueobject.UserId;
import com.valdifly.utils.TimeUtils;

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
