package com.valdifly.infrastructure.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.valdifly.infrastructure.error.ErrorCode;

import java.util.Objects;

/**
 * Uniform success envelope for service responses. Wire shape:
 *
 * <pre>{@code
 * { "success": true,  "data": { ... } }
 * { "success": false, "error": { "code": "NOT_FOUND", "message": "...", "traceId": "..." } }
 * }</pre>
 *
 * Exactly one of {@code data} / {@code error} is set, so the other is omitted
 * (NON_NULL) rather than emitted as null. New endpoints should return
 * {@code ApiResponse.ok(data)} directly; unwrapped DTOs from existing
 * endpoints migrate at their own pace — both shapes are valid until a
 * frontend sweep adopts the envelope.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, ErrorBody error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, null, ErrorBody.of(errorCode, message));
    }

    public static <T> ApiResponse<T> failure(ErrorBody errorBody) {
        return new ApiResponse<>(false, null, Objects.requireNonNull(errorBody, "errorBody must not be null"));
    }
}
