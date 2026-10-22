package com.valdifly.infrastructure.web.spring;

import com.valdifly.domain.common.AggregateNotFoundException;
import com.valdifly.infrastructure.error.BusinessException;
import com.valdifly.infrastructure.error.CommonErrorCode;
import com.valdifly.infrastructure.web.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BaseRestExceptionHandlerTest {

    /** Concrete stand-in for a service's @RestControllerAdvice subclass. */
    static final class TestHandler extends BaseRestExceptionHandler {}

    private final TestHandler handler = new TestHandler();

    @Test
    void businessExceptionMapsHttpStatusFromCode() {
        BusinessException exception = new BusinessException(
                CommonErrorCode.BUSINESS_RULE_VIOLATION, "run already finished");

        ResponseEntity<ApiResponse<Void>> response = handler.business(exception);

        assertEquals(422, response.getStatusCode().value());
        assertEquals(42201, response.getBody().code());
        assertEquals("run already finished", response.getBody().message());
        assertNull(response.getBody().data());
    }

    @Test
    void illegalArgumentMapsTo400() {
        ResponseEntity<ApiResponse<Void>> response = handler.badRequest(new IllegalArgumentException("bad id"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals(40001, response.getBody().code());
        assertEquals("bad id", response.getBody().message());
    }

    @Test
    void illegalStateMapsTo409() {
        ResponseEntity<ApiResponse<Void>> response = handler.conflict(new IllegalStateException("already done"));

        assertEquals(409, response.getStatusCode().value());
        assertEquals(40901, response.getBody().code());
    }

    @Test
    void aggregateNotFoundMapsTo404() {
        ResponseEntity<ApiResponse<Void>> response = handler.notFound(
                new AggregateNotFoundException("Project", "p-1"));

        assertEquals(404, response.getStatusCode().value());
        assertEquals(40401, response.getBody().code());
    }

    @Test
    void unexpectedMapsToLogged500WithStableMessage() {
        ResponseEntity<ApiResponse<Void>> response = handler.unexpected(new RuntimeException("db down"));

        assertEquals(500, response.getStatusCode().value());
        assertEquals(50001, response.getBody().code());
        assertEquals("Internal error", response.getBody().message());
    }

    @Test
    void validationFailureListsFieldReasons() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "command");
        bindingResult.addError(new FieldError("command", "name", "must not be blank"));
        bindingResult.addError(new FieldError("command", "size", "must be positive"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                new MethodParameter(Sample.class.getMethod("accept", String.class), 0), bindingResult);

        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(
                exception, new HttpHeaders(), HttpStatusCode.valueOf(400), (WebRequest) null);

        assertEquals(400, response.getStatusCode().value());
        ApiResponse<?> body = (ApiResponse<?>) response.getBody();
        assertEquals(40002, body.code());
        assertEquals("name: must not be blank; size: must be positive", body.message());
    }

    @Test
    void frameworkExceptionUsesStableMessageForStatus() {
        ResponseEntity<Object> response = handler.handleExceptionInternal(
                new RuntimeException("parser internals leaked here"), null,
                new HttpHeaders(), HttpStatusCode.valueOf(400), (WebRequest) null);

        assertEquals(400, response.getStatusCode().value());
        ApiResponse<?> body = (ApiResponse<?>) response.getBody();
        assertEquals(40001, body.code());
        assertEquals("Malformed request", body.message());
    }

    @SuppressWarnings("unused")
    static class Sample {
        public void accept(String value) {}
    }
}
