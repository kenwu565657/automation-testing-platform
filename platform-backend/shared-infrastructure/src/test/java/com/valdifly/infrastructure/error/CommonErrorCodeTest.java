package com.valdifly.infrastructure.error;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonErrorCodeTest {

    @Test
    void codesAreUnique() {
        Set<Integer> codes = new HashSet<>();
        for (CommonErrorCode value : CommonErrorCode.values()) {
            assertTrue(codes.add(value.code()), "duplicate code: " + value.code());
        }
    }

    @Test
    void successIsZeroAndMapsTo200() {
        assertEquals(0, CommonErrorCode.SUCCESS.code());
        assertEquals(200, CommonErrorCode.SUCCESS.httpStatus());
    }

    @Test
    void httpStatusDerivesFromCodePrefix() {
        for (CommonErrorCode value : CommonErrorCode.values()) {
            if (value == CommonErrorCode.SUCCESS) {
                continue;
            }
            int status = value.httpStatus();
            assertTrue(status >= 400 && status < 600, value + " maps to " + status + ", not a 4xx/5xx");
            assertEquals(status, value.code() / 100, value + " must be <HTTP status><2-digit sequence>");
        }
    }

    @Test
    void statusMatchesDocumentedContract() {
        assertEquals(400, CommonErrorCode.BAD_REQUEST.httpStatus());
        assertEquals(400, CommonErrorCode.VALIDATION_FAILED.httpStatus());
        assertEquals(401, CommonErrorCode.UNAUTHENTICATED.httpStatus());
        assertEquals(403, CommonErrorCode.FORBIDDEN.httpStatus());
        assertEquals(404, CommonErrorCode.NOT_FOUND.httpStatus());
        assertEquals(409, CommonErrorCode.CONFLICT.httpStatus());
        assertEquals(422, CommonErrorCode.BUSINESS_RULE_VIOLATION.httpStatus());
        assertEquals(429, CommonErrorCode.RATE_LIMITED.httpStatus());
        assertEquals(500, CommonErrorCode.INTERNAL_ERROR.httpStatus());
        assertEquals(502, CommonErrorCode.UPSTREAM_FAILURE.httpStatus());
        assertEquals(503, CommonErrorCode.SERVICE_UNAVAILABLE.httpStatus());
    }

    @Test
    void documentedAnchorCodesAreStable() {
        // Published examples from the contract — renumbering them breaks clients.
        assertEquals(40301, CommonErrorCode.FORBIDDEN.code());
        assertEquals(50001, CommonErrorCode.INTERNAL_ERROR.code());
        assertEquals(50002, CommonErrorCode.DB_ERROR.code());
    }

    @Test
    void coversTheStatusesServicesMapToday() {
        // The RestExceptionHandler statuses (400/401/403/404/405/406/409/415/429/500/502/503)
        // plus the Ktor StatusPages trio (400/404/500) must all be expressible.
        Set<Integer> covered = new HashSet<>(Arrays.stream(CommonErrorCode.values())
                .map(CommonErrorCode::httpStatus)
                .toList());
        for (int expected : new int[]{400, 401, 403, 404, 405, 406, 409, 415, 422, 429, 500, 502, 503}) {
            assertTrue(covered.contains(expected), "no common code maps to " + expected);
        }
    }
}
