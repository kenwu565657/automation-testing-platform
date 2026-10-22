package com.valdifly.infrastructure.logging.api;

import com.valdifly.infrastructure.logging.LogFields;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApiAccessLoggerTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void headerWinsOverTraceId() {
        assertEquals("caller-id", ApiAccessLogger.resolveCorrelationId("caller-id", "7f3a"));
    }

    @Test
    void blankHeaderFallsBackToTraceId() {
        assertEquals("7f3a", ApiAccessLogger.resolveCorrelationId("  ", "7f3a"));
        assertEquals("7f3a", ApiAccessLogger.resolveCorrelationId(null, "7f3a"));
    }

    @Test
    void generatesUuidWhenNeitherPresent() {
        String resolved = ApiAccessLogger.resolveCorrelationId(null, null);

        assertNotNull(resolved);
        assertEquals(36, resolved.length(), "expected a canonical UUID");
    }

    @Test
    void emitRestoresPreviousMdcValues() {
        MDC.put(LogFields.TRACE_ID, "previous-trace");
        MDC.put(LogFields.CORRELATION_ID, "previous-correlation");

        ApiAccessLogger.emit(new ApiRequestLog(
                "GET", "/api/v1/project", 200, 12, "127.0.0.1", "test-agent",
                "new-trace", null, "new-correlation", "user-1"));

        assertEquals("previous-trace", MDC.get(LogFields.TRACE_ID));
        assertEquals("previous-correlation", MDC.get(LogFields.CORRELATION_ID));
        assertNull(MDC.get(LogFields.USER_ID), "fields without a previous value must be removed, not left behind");
        assertNull(MDC.get(LogFields.HTTP_METHOD));
        assertNull(MDC.get(LogFields.DURATION_MS));
    }

    @Test
    void emitToleratesNullFields() {
        ApiAccessLogger.emit(new ApiRequestLog(
                "POST", "/api/v1/engines", 204, 3, null, null, null, null, null, null));

        assertNull(MDC.get(LogFields.CLIENT_IP));
        assertNull(MDC.get(LogFields.USER_AGENT));
        assertNull(MDC.get(LogFields.TRACE_ID));
        assertNull(MDC.get(LogFields.CORRELATION_ID));
        assertNull(MDC.get(LogFields.USER_ID));
    }
}
