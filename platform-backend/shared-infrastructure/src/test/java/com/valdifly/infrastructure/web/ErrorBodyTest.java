package com.valdifly.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.valdifly.infrastructure.error.CommonErrorCode;
import com.valdifly.infrastructure.json.ObjectMapperFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErrorBodyTest {

    @Test
    void carriesCodeAndMessage() {
        ErrorBody body = ErrorBody.of(CommonErrorCode.NOT_FOUND, "TestCase tc-1 does not exist", "trace-1");
        assertEquals("NOT_FOUND", body.code());
        assertEquals("TestCase tc-1 does not exist", body.message());
        assertEquals("trace-1", body.traceId());
    }

    @Test
    void picksTraceIdUpFromMdc() {
        try (var ignored = MDC.putCloseable("traceId", "trace-from-mdc")) {
            assertEquals("trace-from-mdc", ErrorBody.of(CommonErrorCode.CONFLICT, "state").traceId());
        }
        assertNull(ErrorBody.of(CommonErrorCode.CONFLICT, "state").traceId());
    }

    @Test
    void blankMessageFallsBackToWireCode() {
        assertEquals("CONFLICT", ErrorBody.of(CommonErrorCode.CONFLICT, null).message());
        assertEquals("CONFLICT", ErrorBody.of(CommonErrorCode.CONFLICT, "  ").message());
    }

    @Test
    void nullFieldsRejected() {
        assertThrows(NullPointerException.class, () -> new ErrorBody(null, "message", null));
        assertThrows(NullPointerException.class, () -> new ErrorBody("CODE", null, null));
    }

    @Test
    void serializesWithoutNullTraceId() throws Exception {
        JsonNode json = ObjectMapperFactory.getInstance().readTree(
                ObjectMapperFactory.getInstance().writeValueAsString(
                        ErrorBody.of(CommonErrorCode.BAD_REQUEST, "name cannot be blank")));

        assertEquals("BAD_REQUEST", json.get("code").asText());
        assertEquals("name cannot be blank", json.get("message").asText());
        assertFalse(json.has("traceId"), "traceId must be omitted, not null");
    }
}
