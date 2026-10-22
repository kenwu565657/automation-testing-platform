package com.valdifly.infrastructure.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.valdifly.infrastructure.error.CommonErrorCode;
import com.valdifly.infrastructure.error.ErrorCode;
import com.valdifly.infrastructure.logging.LogFields;
import org.slf4j.MDC;

/**
 * The single response envelope every endpoint returns, success or failure.
 * Wire shape:
 *
 * <pre>{@code
 * { "code": 0,     "message": "success",   "data": { ... }, "traceId": "7f3a..." }
 * { "code": 40401, "message": "Not found",                  "traceId": "7f3a..." }
 * }</pre>
 *
 * Exactly one of {@code data} / failure is set: successes carry {@code code 0}
 * and {@code data}; failures carry a numeric {@link ErrorCode} and no
 * {@code data}. {@code traceId} is filled from the MDC when present
 * (micrometer-tracing puts it there on Spring services) and omitted otherwise —
 * never emitted as null on any stack, hence the class-level NON_NULL.
 *
 * HTTP status stays semantic (201 for created, 404 for missing, ...); the body
 * {@code code} carries the machine-readable detail. Controllers return this
 * type directly — enforced by an ArchUnit rule in this module's test fixtures.
 */
public record ApiResponse<T>(int code, String message, T data, String traceId) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(CommonErrorCode.SUCCESS.code(), CommonErrorCode.SUCCESS.defaultMessage(), data, MDC.get(LogFields.TRACE_ID));
    }

    public static ApiResponse<Void> ok() {
        return ok(null);
    }

    /**
     * Creates the failure body for an error code, attaching the current trace id
     * when one is on the MDC. A null/blank message falls back to the code's
     * default message so exceptions without messages still render.
     */
    public static <T> ApiResponse<T> failure(ErrorCode errorCode, String message) {
        return new ApiResponse<>(errorCode.code(), orDefault(errorCode, message), null, MDC.get(LogFields.TRACE_ID));
    }

    private static String orDefault(ErrorCode errorCode, String message) {
        return message == null || message.isBlank() ? errorCode.defaultMessage() : message;
    }
}
