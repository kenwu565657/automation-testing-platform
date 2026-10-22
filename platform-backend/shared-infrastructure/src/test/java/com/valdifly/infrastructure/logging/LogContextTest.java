package com.valdifly.infrastructure.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LogContextTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void fieldsArePresentInsideTheScope() {
        try (var ignored = LogContext.open("corr-1", "trace-1")) {
            assertEquals("corr-1", MDC.get(LogFields.CORRELATION_ID));
            assertEquals("trace-1", MDC.get(LogFields.TRACE_ID));
        }
    }

    @Test
    void fieldsAreRemovedOnCloseWhenThereWasNoPreviousValue() {
        LogContext.open("corr-1", "trace-1").close();

        assertNull(MDC.get(LogFields.CORRELATION_ID));
        assertNull(MDC.get(LogFields.TRACE_ID));
    }

    @Test
    void previousValuesAreRestoredOnClose() {
        MDC.put(LogFields.CORRELATION_ID, "outer-correlation");

        try (var ignored = LogContext.open("inner-correlation", "trace-1")) {
            assertEquals("inner-correlation", MDC.get(LogFields.CORRELATION_ID));
        }

        assertEquals("outer-correlation", MDC.get(LogFields.CORRELATION_ID));
    }

    @Test
    void nullArgumentsLeaveKeysUntouched() {
        MDC.put(LogFields.TRACE_ID, "existing-trace");

        try (var ignored = LogContext.open(null, null, "user-1")) {
            assertNull(MDC.get(LogFields.CORRELATION_ID));
            assertEquals("existing-trace", MDC.get(LogFields.TRACE_ID));
            assertEquals("user-1", MDC.get(LogFields.USER_ID));
        }

        assertEquals("existing-trace", MDC.get(LogFields.TRACE_ID));
        assertNull(MDC.get(LogFields.USER_ID));
    }
}
