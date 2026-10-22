package com.valdifly.infrastructure.logging.api;

/**
 * One inbound HTTP request, captured for the access log. Framework adapters
 * (servlet filter, Vert.x route handler, Ktor plugin, WebFlux WebFilter) each
 * build this from their request/response types and hand it to
 * {@link ApiAccessLogger#emit(ApiRequestLog)} — the record is the
 * framework-neutral contract between them.
 *
 * Any field may be null except {@code method} and {@code path}; null fields are
 * simply absent from the emitted log line (LogstashEncoder omits missing MDC
 * entries, so the JSON has no null-valued keys).
 */
public record ApiRequestLog(
        String method,
        String path,
        int status,
        long durationMs,
        String clientIp,
        String userAgent,
        String traceId,
        String spanId,
        String correlationId,
        String userId
) {
}
