package com.valdifly.infrastructure.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valdifly.domain.common.Priority;
import com.valdifly.domain.environment.valueobject.EnvironmentId;
import com.valdifly.domain.execution.event.TestExecutionRequestEvent;
import com.valdifly.domain.execution.valueobject.TestRunId;
import com.valdifly.domain.target.valueobject.ExecutionTargetId;
import com.valdifly.domain.testcase.valueobject.TestCaseId;
import com.valdifly.domain.testcase.valueobject.TestType;
import com.valdifly.domain.testsuite.valueobject.TestSuiteId;
import com.valdifly.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ObjectMapperFactoryTest {

    @Test
    void testGetInstanceReturnsSameInstance() {
        ObjectMapper instance1 = ObjectMapperFactory.getInstance();
        ObjectMapper instance2 = ObjectMapperFactory.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    void testCreateReturnsNewInstance() {
        ObjectMapper instance1 = ObjectMapperFactory.create();
        ObjectMapper instance2 = ObjectMapperFactory.create();
        assertNotSame(instance1, instance2);
    }

    @Test
    void testJacksonConfiguration() throws Exception {
        ObjectMapper mapper = ObjectMapperFactory.getInstance();

        Instant timestamp = Instant.parse("2023-10-01T12:00:00Z");
        TestClass obj = new TestClass();
        obj.setTimestamp(timestamp);
        obj.setCamelCaseField("value");

        String json = mapper.writeValueAsString(obj);

        // Verify Instant is formatted as ISO-8601 string, not timestamp
        assertTrue(json.contains("\"2023-10-01T12:00:00Z\""));
        // Verify camelCase property exists
        assertTrue(json.contains("\"camelCaseField\":\"value\""));
        // Verify null fields are omitted
        assertFalse(json.contains("\"nullField\""));

        // Test unknown properties (FAIL_ON_UNKNOWN_PROPERTIES = false)
        String jsonWithUnknown = "{\"timestamp\":\"2023-10-01T12:00:00Z\",\"camelCaseField\":\"value\",\"unknownField\":\"unknown\"}";
        TestClass deserialized = mapper.readValue(jsonWithUnknown, TestClass.class);

        assertEquals(timestamp, deserialized.getTimestamp());
        assertEquals("value", deserialized.getCamelCaseField());
    }

    @Test
    void valueObjectsStayFlatStringsOnTheWire() throws Exception {
        ObjectMapper mapper = ObjectMapperFactory.getInstance();
        TestExecutionRequestEvent event = new TestExecutionRequestEvent(
                TestRunId.of("run-1"), TestCaseId.of("case-1"), TestSuiteId.of("suite-1"),
                EnvironmentId.of("env-1"), ExecutionTargetId.of("target-1"), UserId.of("qa"),
                TestType.API, Priority.HIGH, 30, 1, 1, Instant.parse("2026-01-01T00:00:00Z")
        );

        String json = mapper.writeValueAsString(event);
        assertTrue(json.contains("\"runId\":\"run-1\""));
        assertTrue(json.contains("\"testCaseId\":\"case-1\""));
        assertTrue(json.contains("\"triggeredBy\":\"qa\""));
        assertFalse(json.contains("\"value\""));

        TestExecutionRequestEvent restored = mapper.readValue(json, TestExecutionRequestEvent.class);
        assertEquals(event.runId(), restored.runId());
        assertEquals(event.testCaseId(), restored.testCaseId());
        assertEquals(event.triggeredBy(), restored.triggeredBy());
    }

    static class TestClass {
        private Instant timestamp;
        private String camelCaseField;
        private String nullField;

        public Instant getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
        }

        public String getCamelCaseField() {
            return camelCaseField;
        }

        public void setCamelCaseField(String camelCaseField) {
            this.camelCaseField = camelCaseField;
        }

        public String getNullField() {
            return nullField;
        }

        public void setNullField(String nullField) {
            this.nullField = nullField;
        }
    }
}

