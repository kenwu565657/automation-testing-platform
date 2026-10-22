package com.valdifly.infrastructure.web.spring;

import com.valdifly.domain.common.AggregateNotFoundException;
import com.valdifly.infrastructure.error.BusinessException;
import com.valdifly.infrastructure.error.CommonErrorCode;
import com.valdifly.infrastructure.error.ErrorCode;
import com.valdifly.infrastructure.web.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

/**
 * Standard error surface for Spring MVC services: every failure leaves as one
 * {@link ApiResponse} envelope. Each service subclasses this and annotates the
 * subclass with {@code @RestControllerAdvice} (component scanning only sees the
 * service's own package, so the base class cannot register itself).
 *
 * Mappings provided here:
 * <ul>
 *   <li>{@code MethodArgumentNotValidException} → 400 {@code VALIDATION_FAILED},
 *       with one {@code "field: reason"} per violation;</li>
 *   <li>{@link BusinessException} → HTTP status derived from its code
 *       ({@code code / 100});</li>
 *   <li>domain guards per DDD_STANDARDS.md §5/§6 — {@code IllegalArgumentException}
 *       → 400, {@code IllegalStateException} → 409, {@code AggregateNotFoundException}
 *       → 404;</li>
 *   <li>Spring MVC framework exceptions (malformed JSON, wrong method, ...) → the
 *       code matching the framework status, with a stable per-status message;</li>
 *   <li>anything left over → logged 500 {@code INTERNAL_ERROR}.</li>
 * </ul>
 *
 * Subclasses add {@code @ExceptionHandler} methods for their own application
 * exceptions and build bodies with {@link #failure(ErrorCode, String)} — never
 * re-declare handlers for the exceptions above (ambiguous mapping at startup).
 */
public abstract class BaseRestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(BaseRestExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> business(BusinessException exception) {
        return failure(exception.errorCode(), exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> badRequest(IllegalArgumentException exception) {
        return failure(CommonErrorCode.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> conflict(IllegalStateException exception) {
        return failure(CommonErrorCode.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(AggregateNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> notFound(AggregateNotFoundException exception) {
        return failure(CommonErrorCode.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> unexpected(Exception exception) {
        log.error("Unhandled exception", exception);
        return failure(CommonErrorCode.INTERNAL_ERROR, null);
    }

    /** Builds the failure response with the HTTP status derived from the code. */
    protected final ResponseEntity<ApiResponse<Void>> failure(ErrorCode errorCode, @Nullable String message) {
        return ResponseEntity.status(errorCode.httpStatus()).body(ApiResponse.failure(errorCode, message));
    }

    /** Bean-validation failures → 400 with one "field: reason" per violation. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(CommonErrorCode.VALIDATION_FAILED, message));
    }

    /**
     * Framework exceptions (malformed JSON, type mismatches, ...) carry parser
     * internals in their messages — answer with the code's stable default
     * message instead.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(@NonNull Exception exception, @Nullable Object body, @NonNull HttpHeaders headers, HttpStatusCode statusCode, @NonNull WebRequest request) {
        ApiResponse<Void> error = ApiResponse.failure(codeFor(statusCode.value()), null);
        return super.handleExceptionInternal(exception, error, headers, statusCode, request);
    }

    private static CommonErrorCode codeFor(int status) {
        return switch (status) {
            case 400 -> CommonErrorCode.BAD_REQUEST;
            case 401 -> CommonErrorCode.UNAUTHENTICATED;
            case 403 -> CommonErrorCode.FORBIDDEN;
            case 404 -> CommonErrorCode.NOT_FOUND;
            case 405 -> CommonErrorCode.METHOD_NOT_ALLOWED;
            case 406 -> CommonErrorCode.NOT_ACCEPTABLE;
            case 409 -> CommonErrorCode.CONFLICT;
            case 415 -> CommonErrorCode.UNSUPPORTED_MEDIA_TYPE;
            default -> CommonErrorCode.INTERNAL_ERROR;
        };
    }
}
