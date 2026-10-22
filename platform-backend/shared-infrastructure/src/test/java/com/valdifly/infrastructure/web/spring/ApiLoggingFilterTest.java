package com.valdifly.infrastructure.web.spring;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.valdifly.infrastructure.logging.LogFields;
import com.valdifly.infrastructure.logging.api.ApiAccessLogger;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiLoggingFilterTest {

    private ListAppender<ILoggingEvent> events;

    @BeforeEach
    void captureAccessLog() {
        Logger logger = (Logger) LoggerFactory.getLogger(ApiAccessLogger.class);
        events = new ListAppender<>();
        events.start();
        logger.addAppender(events);
    }

    @AfterEach
    void stopCapture() {
        Logger logger = (Logger) LoggerFactory.getLogger(ApiAccessLogger.class);
        logger.detachAppender(events);
        MDC.clear();
    }

    @Test
    void emitsOneStructuredLinePerRequest() throws Exception {
        var filter = new ApiLoggingFilter(Set.of());
        var request = new MockHttpServletRequest("GET", "/api/v1/project");
        request.setRemoteAddr("10.0.0.9");
        request.addHeader("User-Agent", "test-agent");
        var response = new MockHttpServletResponse();
        MDC.put(LogFields.TRACE_ID, "trace-123"); // what micrometer-tracing's outer filter does

        filter.doFilter(request, response, chain((req, res) -> {
            req.setAttribute(ApiLoggingFilter.USER_ID_ATTRIBUTE, "user-7"); // what the auth filter does
            ((MockHttpServletResponse) res).setStatus(201);
        }));

        assertEquals(1, events.list.size());
        var mdc = events.list.get(0).getMDCPropertyMap();
        assertEquals("GET", mdc.get(LogFields.HTTP_METHOD));
        assertEquals("/api/v1/project", mdc.get(LogFields.HTTP_PATH));
        assertEquals("201", mdc.get(LogFields.HTTP_STATUS));
        assertNotNull(mdc.get(LogFields.DURATION_MS));
        assertEquals("10.0.0.9", mdc.get(LogFields.CLIENT_IP));
        assertEquals("test-agent", mdc.get(LogFields.USER_AGENT));
        assertEquals("trace-123", mdc.get(LogFields.TRACE_ID));
        assertEquals("trace-123", mdc.get(LogFields.CORRELATION_ID), "no header -> trace id becomes the correlation id");
        assertEquals("user-7", mdc.get(LogFields.USER_ID));
    }

    @Test
    void correlationHeaderWinsAndIsVisibleDownstream() throws Exception {
        var filter = new ApiLoggingFilter(Set.of());
        var request = new MockHttpServletRequest("POST", "/api/v1/project");
        request.addHeader(ApiAccessLogger.CORRELATION_ID_HEADER, "caller-corr");
        var response = new MockHttpServletResponse();
        String[] downstreamCorrelation = new String[1];

        filter.doFilter(request, response, chain((req, res) ->
                downstreamCorrelation[0] = MDC.get(LogFields.CORRELATION_ID)));

        assertEquals("caller-corr", downstreamCorrelation[0], "application logs must carry the correlation id");
        assertEquals("caller-corr", events.list.get(0).getMDCPropertyMap().get(LogFields.CORRELATION_ID));
        assertNull(MDC.get(LogFields.CORRELATION_ID), "MDC must be clean after the request — no leakage across pooled threads");
    }

    @Test
    void prefersFirstXForwardedForEntry() throws Exception {
        var filter = new ApiLoggingFilter(Set.of());
        var request = new MockHttpServletRequest("GET", "/api/v1/project");
        request.addHeader("X-Forwarded-For", "203.0.113.5, 10.0.0.9");
        request.setRemoteAddr("10.0.0.9");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain((req, res) -> {}));

        assertEquals("203.0.113.5", events.list.get(0).getMDCPropertyMap().get(LogFields.CLIENT_IP));
    }

    @Test
    void skipsExcludedPaths() throws Exception {
        var filter = new ApiLoggingFilter(Set.of("/actuator", "/health"));
        var request = new MockHttpServletRequest("GET", "/actuator/health");
        var response = new MockHttpServletResponse();
        boolean[] chainRan = {false};

        filter.doFilter(request, response, chain((req, res) -> chainRan[0] = true));

        assertTrue(chainRan[0], "excluded requests still pass through");
        assertTrue(events.list.isEmpty(), "excluded paths emit no access log");
    }

    @Test
    void emitsEvenWhenTheChainThrows() {
        var filter = new ApiLoggingFilter(Set.of());
        var request = new MockHttpServletRequest("GET", "/api/v1/project");
        var response = new MockHttpServletResponse();

        assertThrows(RuntimeException.class, () ->
                filter.doFilter(request, response, chain((req, res) -> {
                    throw new RuntimeException("boom");
                })));

        assertEquals(1, events.list.size(), "the access log is emitted from the finally block even on failure");
        assertNull(MDC.get(LogFields.CORRELATION_ID));
    }

    private static FilterChain chain(ThrowingEndpoint endpoint) {
        return (request, response) -> endpoint.run(request, response);
    }

    private interface ThrowingEndpoint {
        void run(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response);
    }
}
