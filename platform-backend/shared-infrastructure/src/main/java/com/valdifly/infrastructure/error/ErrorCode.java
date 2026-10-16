package com.valdifly.infrastructure.error;

/**
 * Machine-readable error contract shared by every service.
 *
 * Codes are numeric and carry their HTTP status in their prefix: a code is
 * {@code <3-digit HTTP status><2-digit sequence>} — {@code 40301} maps to
 * HTTP 403, {@code 42201} to HTTP 422. The single exception is
 * {@code SUCCESS = 0}, which maps to HTTP 200. Ranges:
 *
 * <ul>
 *   <li>{@code 0} — success;</li>
 *   <li>{@code 4xxxx} — client-side error ({@code 401xx} auth, {@code 404xx} not found);</li>
 *   <li>{@code 422xx} — business rule violated (request was well-formed);</li>
 *   <li>{@code 5xxxx} — server-side error.</li>
 * </ul>
 *
 * Two flavors exist by design:
 * <ul>
 *   <li>{@link CommonErrorCode} — the platform-wide vocabulary shared by all services;</li>
 *   <li>per-service enums (e.g. {@code AdminErrorCode}) for domain-specific failures,
 *       implementing this interface so every transport layer renders them identically.
 *       Service codes must claim a free sequence inside the matching range.</li>
 * </ul>
 *
 * Domain layers keep throwing plain JDK exceptions ({@code IllegalArgumentException} /
 * {@code IllegalStateException}) and {@code AggregateNotFoundException}; the inbound
 * adapter maps them to an {@code ErrorCode} at the edge — the domain never knows about
 * codes or statuses (DDD_STANDARDS.md §6). Application services may also throw
 * {@link BusinessException} to signal a coded failure directly.
 */
public interface ErrorCode {

    /** Stable wire code, e.g. {@code 40401}. Never reused or renumbered once published. */
    int code();

    /** Fallback message when the failure carries no specific one, e.g. {@code "Not found"}. */
    String defaultMessage();

    /** HTTP status this error maps to — derived from the code prefix ({@code 40301 → 403}). */
    default int httpStatus() {
        return code() == 0 ? 200 : code() / 100;
    }
}
