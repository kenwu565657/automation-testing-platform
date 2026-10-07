package com.valdifly.admin.adapter.inbound.rest.shared;

import com.valdifly.admin.application.access.AccessDeniedException;
import com.valdifly.domain.common.AggregateNotFoundException;
import com.valdifly.admin.application.ai.LanguageModelException;
import com.valdifly.admin.application.auth.InvalidCredentialsException;
import com.valdifly.infrastructure.error.CommonErrorCode;
import com.valdifly.infrastructure.web.ErrorBody;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler({InvalidCredentialsException.class, AuthenticationException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorBody unauthorized(RuntimeException exception) {
        return ErrorBody.of(CommonErrorCode.UNAUTHENTICATED,
                exception.getMessage() == null ? "Authentication required" : exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorBody forbidden(AccessDeniedException exception) {
        return ErrorBody.of(CommonErrorCode.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(AggregateNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorBody aggregateNotFound(AggregateNotFoundException exception) {
        return ErrorBody.of(CommonErrorCode.NOT_FOUND, exception.getMessage());
    }


    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorBody badRequest(IllegalArgumentException exception) {
        return ErrorBody.of(CommonErrorCode.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorBody conflict(IllegalStateException exception) {
        return ErrorBody.of(CommonErrorCode.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(LanguageModelException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorBody languageModel(LanguageModelException exception) {
        return ErrorBody.of(CommonErrorCode.UPSTREAM_FAILURE, exception.getMessage());
    }
}
