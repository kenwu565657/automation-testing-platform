package com.valdifly.infrastructure.web.spring;

import com.valdifly.infrastructure.logging.LogFields;
import com.valdifly.infrastructure.logging.api.ApiAccessLogger;
import com.valdifly.infrastructure.logging.api.ApiRequestLog;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * API access-log filter for Spring MVC services — the servlet-stack
 * interception point for the shared {@link ApiAccessLogger}. Emits exactly one
 * structured line per request, and seeds {@code correlationId} on the MDC for
 * the whole downstream chain so application logs carry it too.
 *
 * Registration guidance (per service, as a {@code FilterRegistrationBean}):
 * order {@code -200} — after Spring Boot's tracing observation filter (so
 * {@code traceId}/{@code spanId} are already on the MDC) and outside the
 * Spring Security chain ({@code -100}), so 401/403 rejections are logged too.
 *
 * Two cooperating contracts:
 * <ul>
 *   <li>{@code userId} is read from the request attribute {@link #USER_ID_ATTRIBUTE},
 *       not the MDC — the authentication filter clears its MDC value before this
 *       outer filter emits (mirrors the {@code AUTH_FAILURE_ATTRIBUTE} pattern);</li>
 *   <li>{@code traceId}/{@code spanId} are read from the MDC after the chain —
 *       still populated, because the tracing filter wraps this one.</li>
 * </ul>
 *
 * Client IP prefers the first {@code X-Forwarded-For} entry (traffic arrives
 * via the gateway) and falls back to the direct remote address.
 */
public class ApiLoggingFilter extends OncePerRequestFilter {

    /** Request attribute carrying the authenticated user id for the access log. */
    public static final String USER_ID_ATTRIBUTE = ApiLoggingFilter.class.getName() + ".userId";

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String USER_AGENT_HEADER = "User-Agent";

    private final Set<String> excludedPrefixes;

    public ApiLoggingFilter(Set<String> excludedPrefixes) {
        this.excludedPrefixes = Set.copyOf(excludedPrefixes);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return excludedPrefixes.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        long startNanos = System.nanoTime();
        String correlationId = ApiAccessLogger.resolveCorrelationId(request.getHeader(ApiAccessLogger.CORRELATION_ID_HEADER), MDC.get(LogFields.TRACE_ID));
        MDC.put(LogFields.CORRELATION_ID, correlationId);
        try {
            chain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            try {
                ApiAccessLogger.emit(new ApiRequestLog(
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        durationMs,
                        clientIp(request),
                        request.getHeader(USER_AGENT_HEADER),
                        MDC.get(LogFields.TRACE_ID),
                        MDC.get(LogFields.SPAN_ID),
                        correlationId,
                        userId(request)));
            } finally {
                MDC.remove(LogFields.CORRELATION_ID);
            }
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader(X_FORWARDED_FOR);
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static String userId(HttpServletRequest request) {
        Object attribute = request.getAttribute(USER_ID_ATTRIBUTE);
        return attribute instanceof String userId ? userId : null;
    }
}
