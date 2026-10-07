package com.valdifly.infrastructure.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.valdifly.infrastructure.error.ErrorCode;
import com.valdifly.infrastructure.logging.LogFields;
import org.slf4j.MDC;

import java.util.Objects;

/**
 * The single error payload every service returns on failure, replacing the
 * per-service {@code Map.of("error", message)} bodies. Wire shape:
 *
 * <pre>{@code
 * { "code": "NOT_FOUND", "message": "TestCase 123 does not exist", "traceId": "7f3a..." }
 * }</pre>
 *
 * {@code traceId} is filled from the MDC when present (micrometer-tracing puts
 * it there on Spring services) and omitted otherwise — never emitted as null
 * on any stack, hence the class-level NON_NULL.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorBody(String code, String message, String traceId) {

    public ErrorBody {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }

    /**
     * Creates the body for an error code, attaching the current trace id when
     * one is on the MDC. A null/blank message falls back to the wire code so
     * exceptions without messages still render (previously {@code Map.of}
     * would have thrown a NullPointerException and turned a 4xx into a 500).
     */
    public static ErrorBody of(ErrorCode errorCode, String message) {
        return new ErrorBody(errorCode.code(), orCode(errorCode, message), MDC.get(LogFields.TRACE_ID));
    }

    /** Creates the body with an explicit trace id; null/blank falls back to the wire code. */
    public static ErrorBody of(ErrorCode errorCode, String message, String traceId) {
        return new ErrorBody(errorCode.code(), orCode(errorCode, message), traceId);
    }

    private static String orCode(ErrorCode errorCode, String message) {
        return message == null || message.isBlank() ? errorCode.code() : message;
    }
}
