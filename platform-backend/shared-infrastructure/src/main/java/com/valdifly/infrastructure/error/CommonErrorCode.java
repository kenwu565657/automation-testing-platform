package com.valdifly.infrastructure.error;

/**
 * Platform-wide error vocabulary shared by all services.
 *
 * Codes match the values report-service already ships (BAD_REQUEST / NOT_FOUND /
 * INTERNAL_ERROR) so existing clients keep parsing them when services adopt
 * {@code com.valdifly.infrastructure.web.ErrorBody}. Service-specific failures
 * get their own enum implementing
 * {@link ErrorCode} rather than a new constant here.
 */
public enum CommonErrorCode implements ErrorCode {

    BAD_REQUEST("BAD_REQUEST", 400),
    UNAUTHENTICATED("UNAUTHENTICATED", 401),
    FORBIDDEN("FORBIDDEN", 403),
    NOT_FOUND("NOT_FOUND", 404),
    CONFLICT("CONFLICT", 409),
    UPSTREAM_FAILURE("UPSTREAM_FAILURE", 502),
    INTERNAL_ERROR("INTERNAL_ERROR", 500);

    private final String code;
    private final int httpStatus;

    CommonErrorCode(String code, int httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public int httpStatus() {
        return httpStatus;
    }
}
