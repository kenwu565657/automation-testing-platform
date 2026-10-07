package com.valdifly.infrastructure.logging;

/**
 * Canonical MDC key names shared by every service, so structured log fields
 * (and the traceId on {@code com.valdifly.infrastructure.web.ErrorBody}) are
 * spelled identically everywhere — one spelling per field, see
 * DDD_STANDARDS.md §7.
 *
 * {@link #TRACE_ID} and {@link #SPAN_ID} match micrometer-tracing's defaults,
 * which is what puts them on the MDC of Spring services. {@link #CORRELATION_ID}
 * matches the {@code EventMetadata.correlationId} field carried on Kafka events,
 * letting logs tie back to the originating request across services.
 */
public final class LogFields {

    /** OpenTelemetry/micrometer trace id — links log lines to exported spans. */
    public static final String TRACE_ID = "traceId";

    /** OpenTelemetry/micrometer span id. */
    public static final String SPAN_ID = "spanId";

    /** Cross-service correlation id — same value as EventMetadata.correlationId. */
    public static final String CORRELATION_ID = "correlationId";

    /** Authenticated actor id, set by inbound adapters after authentication. */
    public static final String USER_ID = "userId";

    private LogFields() {}
}
