package com.valdifly.domain.testcase.event;

import com.valdifly.domain.common.EventType;
import com.valdifly.domain.common.Priority;
import com.valdifly.domain.project.valueobject.ProjectId;
import com.valdifly.domain.testcase.valueobject.AuthoringStyle;
import com.valdifly.domain.testcase.valueobject.TestCaseId;
import com.valdifly.domain.testcase.valueobject.TestType;
import com.valdifly.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestCaseCreatedEventTest {

    @Test
    void namesTheCreatedEvent() {
        Instant when = Instant.parse("2026-09-19T00:00:00Z");
        TestCaseCreatedEvent event = new TestCaseCreatedEvent(
                TestCaseId.of("case-1"), "Login", ProjectId.of("proj-1"),
                TestType.WEB_E2E, AuthoringStyle.SIMPLE, Priority.HIGH, Set.of("@smoke"),
                UserId.of("qa"), when
        );

        assertEquals(EventType.TEST_CASE_CREATED, event.eventType());
        assertEquals(when, event.occurredAt());
        assertEquals("case-1", event.testCaseId().value());
        assertEquals(Set.of("@smoke"), event.tags());
    }

    @Test
    void defaultsEmptyTagsAndNow() {
        TestCaseCreatedEvent event = new TestCaseCreatedEvent(
                TestCaseId.of("case-1"), "Login", ProjectId.of("proj-1"),
                TestType.API, AuthoringStyle.GHERKIN, Priority.LOW, null,
                UserId.of("qa"), null
        );

        assertEquals(Set.of(), event.tags());
        assertNotNull(event.createdAt());
        assertFalse(event.createdAt().isAfter(Instant.now().plusSeconds(1)));
    }
}
