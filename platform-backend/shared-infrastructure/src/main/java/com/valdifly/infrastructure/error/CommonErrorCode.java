package com.valdifly.infrastructure.error;

/**
 * Platform-wide error vocabulary shared by all services, following the numeric
 * scheme in {@link ErrorCode}: {@code <HTTP status><2-digit sequence>}.
 *
 * Service-specific failures get their own enum implementing {@link ErrorCode}
 * rather than a new constant here.
 */
public enum CommonErrorCode implements ErrorCode {

    SUCCESS(0, "success"),

    // 4xxxx — client-side errors
    BAD_REQUEST(40001, "Malformed request"),
    VALIDATION_FAILED(40002, "Validation failed"),
    UNAUTHENTICATED(40101, "Authentication required"),
    FORBIDDEN(40301, "Forbidden"),
    NOT_FOUND(40401, "Not found"),
    METHOD_NOT_ALLOWED(40501, "Method not allowed"),
    NOT_ACCEPTABLE(40601, "Not acceptable"),
    CONFLICT(40901, "Conflict"),
    UNSUPPORTED_MEDIA_TYPE(41501, "Unsupported media type"),
    RATE_LIMITED(42901, "Rate limit exceeded"),

    // 422xx — business rule violations (well-formed request, domain says no)
    BUSINESS_RULE_VIOLATION(42201, "Business rule violated"),

    // 5xxxx — server-side errors
    INTERNAL_ERROR(50001, "Internal error"),
    DB_ERROR(50002, "Database error"),
    UPSTREAM_FAILURE(50201, "Upstream failure"),
    SERVICE_UNAVAILABLE(50301, "Service unavailable");

    private final int code;
    private final String defaultMessage;

    CommonErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    private static int httpStatusOf(int code) {
        return code / 100;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String defaultMessage() {
        return defaultMessage;
    }
}
