package com.valdifly.infrastructure.logging.api;

import com.valdifly.infrastructure.logging.LogFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The single API access-log emitter shared by every framework adapter. One
 * call = one structured line, e.g.
 *
 * <pre>{@code
 * GET /api/v1/project -> 200 (12 ms)
 * }</pre>
 *
 * emitted at INFO with every {@link ApiRequestLog} field on the MDC, so the
 * shared {@code logback-base.xml} (LogstashEncoder) serializes them as JSON
 * fields with their canonical {@link LogFields} spellings.
 *
 * The emit is <b>atomic</b>: fields are put on the MDC, the line is logged,
 * and the previous MDC values are restored (key removed when there was no
 * previous value) — all within this call, with no suspension point in between.
 * That makes it safe on event-loop, coroutine, and reactor threads where
 * values must never linger on the thread-local MDC between requests.
 */
public final class ApiAccessLogger {

    private static final Logger log = LoggerFactory.getLogger(ApiAccessLogger.class);

    /**
     * Header carrying the caller's correlation id. When absent, the current
     * trace id becomes the correlation id — matching the platform convention
     * where Kafka {@code EventMetadata.correlationId} is the originating HTTP
     * trace id (see engine-service's {@code EngineTracing}).
     */
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private ApiAccessLogger() {}

    /**
     * Resolves the correlation id for a request: the {@value #CORRELATION_ID_HEADER}
     * header when present and non-blank, else the current trace id (linking the
     * request to downstream events), else a freshly generated UUID.
     */
    public static String resolveCorrelationId(String headerValue, String traceId) {
        if (headerValue != null && !headerValue.isBlank()) {
            return headerValue;
        }
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }
        return UUID.randomUUID().toString();
    }

    /**
     * Emits one access-log line with all non-null fields on the MDC, restoring
     * the previous MDC state afterwards. Safe to call on any thread model.
     */
    public static void emit(ApiRequestLog request) {
        List<AppliedField> applied = new ArrayList<>(10);
        apply(applied, LogFields.HTTP_METHOD, request.method());
        apply(applied, LogFields.HTTP_PATH, request.path());
        apply(applied, LogFields.HTTP_STATUS, String.valueOf(request.status()));
        apply(applied, LogFields.DURATION_MS, String.valueOf(request.durationMs()));
        apply(applied, LogFields.CLIENT_IP, request.clientIp());
        apply(applied, LogFields.USER_AGENT, request.userAgent());
        apply(applied, LogFields.TRACE_ID, request.traceId());
        apply(applied, LogFields.SPAN_ID, request.spanId());
        apply(applied, LogFields.CORRELATION_ID, request.correlationId());
        apply(applied, LogFields.USER_ID, request.userId());
        try {
            log.info("{} {} -> {} ({} ms)", request.method(), request.path(), request.status(), request.durationMs());
        } finally {
            for (int i = applied.size() - 1; i >= 0; i--) {
                applied.get(i).restore();
            }
        }
    }

    private static void apply(List<AppliedField> applied, String key, String value) {
        if (value != null) {
            applied.add(new AppliedField(key, MDC.get(key)));
            MDC.put(key, value);
        }
    }

    /** One MDC key that was replaced, remembering what to put back. */
    private record AppliedField(String key, String previousValue) {

        void restore() {
            if (previousValue == null) {
                MDC.remove(key);
            } else {
                MDC.put(key, previousValue);
            }
        }
    }
}
