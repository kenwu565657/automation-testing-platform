package com.valdifly.domain.testcase.event;

import com.valdifly.domain.common.EventType;
import com.valdifly.domain.common.Priority;
import com.valdifly.domain.project.valueobject.ProjectId;
import com.valdifly.domain.testcase.valueobject.AuthoringStyle;
import com.valdifly.domain.testcase.valueobject.TestCaseId;
import com.valdifly.domain.testcase.valueobject.TestType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestCaseUpdatedEventTest {

    @Test
    void namesTheUpdatedEvent() {
        Instant when = Instant.parse("2026-09-19T00:00:00Z");
        TestCaseUpdatedEvent event = new TestCaseUpdatedEvent(
                TestCaseId.of("case-1"), "Login", ProjectId.of("proj-1"),
                TestType.API, AuthoringStyle.SIMPLE, Priority.MEDIUM, Set.of("@reg"),
                3, false, when
        );

        assertEquals(EventType.TEST_CASE_UPDATED, event.eventType());
        assertEquals(when, event.occurredAt());
        assertEquals(3, event.latestDefinitionVersion());
        assertFalse(event.active());
    }

    @Test
    void defaultsEmptyTagsAndNow() {
        TestCaseUpdatedEvent event = new TestCaseUpdatedEvent(
                TestCaseId.of("case-1"), "Login", ProjectId.of("proj-1"),
                TestType.LOAD_TEST, AuthoringStyle.SIMPLE, Priority.HIGH, null,
                1, true, null
        );

        assertEquals(Set.of(), event.tags());
        assertNotNull(event.updatedAt());
    }
}
