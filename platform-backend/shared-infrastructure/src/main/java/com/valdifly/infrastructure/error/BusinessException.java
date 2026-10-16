package com.valdifly.infrastructure.error;

/**
 * A failure that carries an {@link ErrorCode} — thrown by application services
 * when no plain JDK exception expresses the failure's wire semantics (e.g. a
 * 422xx business rule violation). The shared exception handler maps it to the
 * HTTP status derived from the code, so use sites never touch HTTP.
 *
 * Domain aggregates still throw plain JDK exceptions
 * ({@code IllegalArgumentException} / {@code IllegalStateException}) and
 * {@code AggregateNotFoundException} per DDD_STANDARDS.md §6 — this type is for
 * the application layer, not the domain.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
