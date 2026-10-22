package com.valdifly.infrastructure.web.vertx;

import com.valdifly.infrastructure.logging.api.ApiAccessLogger;
import com.valdifly.infrastructure.logging.api.ApiRequestLog;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * API access-log route handler for Vert.x services — the Vert.x-stack
 * interception point for the shared {@link ApiAccessLogger}. Register it first
 * on the router so every request passes through:
 *
 * <pre>{@code
 * router.route().handler(ApiLoggingHandler.create(Set.of("/health", "/metrics")));
 * }</pre>
 *
 * The line is emitted from an end handler (fires when the response completes,
 * with or without a body) via {@link ApiAccessLogger#emit}, which is atomic —
 * nothing is ever left on the MDC between requests, because event-loop threads
 * are shared. In-request MDC propagation is deliberately NOT done here; use
 * {@code LogContext} around synchronous units of work instead.
 *
 * {@code traceId} is read opportunistically from the current OTel span (present
 * when the server runs with vertx-opentelemetry); {@code correlationId} follows
 * the shared resolution: {@code X-Correlation-Id} header &gt; trace id &gt;
 * generated UUID.
 */
public final class ApiLoggingHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggingHandler.class);

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String USER_AGENT_HEADER = "User-Agent";

    private ApiLoggingHandler() {}

    public static Handler<RoutingContext> create(Set<String> excludedPrefixes) {
        Set<String> excluded = Set.copyOf(excludedPrefixes);
        return context -> {
            String path = context.request().path();
            if (excluded.stream().anyMatch(path::startsWith)) {
                context.next();
                return;
            }
            long startNanos = System.nanoTime();
            context.addEndHandler(ignored -> {
                try {
                    String traceId = currentTraceId();
                    ApiAccessLogger.emit(new ApiRequestLog(
                            context.request().method().name(),
                            path,
                            context.response().getStatusCode(),
                            (System.nanoTime() - startNanos) / 1_000_000,
                            clientIp(context),
                            context.request().getHeader(USER_AGENT_HEADER),
                            traceId,
                            null,
                            ApiAccessLogger.resolveCorrelationId(
                                    context.request().getHeader(ApiAccessLogger.CORRELATION_ID_HEADER), traceId),
                            null));
                } catch (Throwable loggingFailure) {
                    // Access logging must never break response completion — includes a missing
                    // opentelemetry-api at runtime (compileOnly here; engines ship it via vertx-opentelemetry).
                    log.warn("API access log failed for {} {}", context.request().method().name(), path, loggingFailure);
                }
            });
            context.next();
        };
    }

    private static String currentTraceId() {
        SpanContext spanContext = Span.current().getSpanContext();
        return spanContext.isValid() ? spanContext.getTraceId() : null;
    }

    private static String clientIp(RoutingContext context) {
        String forwarded = context.request().getHeader(X_FORWARDED_FOR);
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return context.request().remoteAddress() == null ? null : context.request().remoteAddress().host();
    }
}
