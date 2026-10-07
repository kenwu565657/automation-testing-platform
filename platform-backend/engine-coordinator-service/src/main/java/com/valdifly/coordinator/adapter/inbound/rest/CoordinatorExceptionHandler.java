package com.valdifly.coordinator.adapter.inbound.rest;

import com.valdifly.infrastructure.error.CommonErrorCode;
import com.valdifly.infrastructure.web.ErrorBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CoordinatorExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorBody badRequest(IllegalArgumentException exception) {
        return ErrorBody.of(CommonErrorCode.BAD_REQUEST, exception.getMessage());
    }
}
