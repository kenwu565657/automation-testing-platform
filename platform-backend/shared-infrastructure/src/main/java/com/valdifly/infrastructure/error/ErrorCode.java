package com.valdifly.infrastructure.error;

/**
 * Machine-readable error contract shared by every service.
 *
 * Implementations pair a stable wire code (client-facing, never renamed once
 * published — one spelling per field, see DDD_STANDARDS.md §7) with the HTTP
 * status it maps to. The status is an {@code int}, not Spring's HttpStatus,
 * because this module serves Spring Boot, Vert.x, and Ktor services alike.
 *
 * Two flavors exist by design:
 * <ul>
 *   <li>{@link CommonErrorCode} — the platform-wide vocabulary shared by all services;</li>
 *   <li>per-service enums (e.g. {@code AdminErrorCode}) for domain-specific failures,
 *       implementing this interface so every transport layer renders them identically.</li>
 * </ul>
 *
 * Domain layers keep throwing plain JDK exceptions ({@code IllegalArgumentException} /
 * {@code IllegalStateException}) and {@code AggregateNotFoundException}; each service's
 * inbound adapter maps them to an {@code ErrorCode} at the edge — the domain never
 * knows about codes or statuses (DDD_STANDARDS.md §6).
 */
public interface ErrorCode {

    /** Stable wire code, e.g. {@code NOT_FOUND}. */
    String code();

    /** HTTP status this error maps to, e.g. {@code 404}. */
    int httpStatus();
}
