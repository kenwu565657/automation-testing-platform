package com.valdifly.infrastructure.logging;

import org.slf4j.MDC;

/**
 * Try-with-resources MDC scope for non-servlet processing — Kafka consumers,
 * Vert.x event-bus handlers, batch jobs — where the {@link LogFields} should
 * ride on every log line of one unit of work:
 *
 * <pre>{@code
 * try (var ignored = LogContext.open(correlationId, traceId)) {
 *     // every log line here carries correlationId + traceId
 * }
 * }</pre>
 *
 * Values are restored on {@link #close()} (previous value back, or key removed
 * when there was none — {@code MDC.putCloseable} only removes, hence the
 * explicit bookkeeping), so fields never leak into the next unit of work on a
 * reused thread. Null arguments leave the key untouched, letting callers scope
 * only what they have. Keep the scope synchronous: MDC is thread-local, so
 * work that suspends or hops threads must open its own scope on the new thread.
 */
public final class LogContext implements AutoCloseable {

    private final ScopedField correlationId;
    private final ScopedField traceId;
    private final ScopedField userId;

    private LogContext(String correlationId, String traceId, String userId) {
        this.correlationId = ScopedField.apply(LogFields.CORRELATION_ID, correlationId);
        this.traceId = ScopedField.apply(LogFields.TRACE_ID, traceId);
        this.userId = ScopedField.apply(LogFields.USER_ID, userId);
    }

    /** Scopes the given correlation and trace ids for the current unit of work. */
    public static LogContext open(String correlationId, String traceId) {
        return new LogContext(correlationId, traceId, null);
    }

    /** Scopes correlation, trace, and user ids for the current unit of work. */
    public static LogContext open(String correlationId, String traceId, String userId) {
        return new LogContext(correlationId, traceId, userId);
    }

    @Override
    public void close() {
        userId.restore();
        traceId.restore();
        correlationId.restore();
    }

    /** One MDC key whose value was replaced, remembering what to put back. */
    private record ScopedField(String key, String previousValue, boolean applied) {

        static ScopedField apply(String key, String value) {
            if (value == null) {
                return new ScopedField(key, null, false);
            }
            String previousValue = MDC.get(key);
            MDC.put(key, value);
            return new ScopedField(key, previousValue, true);
        }

        void restore() {
            if (!applied) {
                return;
            }
            if (previousValue == null) {
                MDC.remove(key);
            } else {
                MDC.put(key, previousValue);
            }
        }
    }
}
