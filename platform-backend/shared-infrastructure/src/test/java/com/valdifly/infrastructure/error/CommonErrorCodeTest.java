package com.valdifly.infrastructure.error;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonErrorCodeTest {

    @Test
    void codesAreUniqueAndNonBlank() {
        Set<String> codes = new HashSet<>();
        for (CommonErrorCode value : CommonErrorCode.values()) {
            assertFalse(value.code().isBlank(), value + " has a blank code");
            assertTrue(codes.add(value.code()), "duplicate code: " + value.code());
        }
    }

    @Test
    void everyCodeIsAnErrorStatus() {
        for (CommonErrorCode value : CommonErrorCode.values()) {
            int status = value.httpStatus();
            assertTrue(status >= 400 && status < 600, value + " maps to " + status + ", not a 4xx/5xx");
        }
    }

    @Test
    void statusMatchesDocumentedContract() {
        assertEquals(400, CommonErrorCode.BAD_REQUEST.httpStatus());
        assertEquals(401, CommonErrorCode.UNAUTHENTICATED.httpStatus());
        assertEquals(403, CommonErrorCode.FORBIDDEN.httpStatus());
        assertEquals(404, CommonErrorCode.NOT_FOUND.httpStatus());
        assertEquals(409, CommonErrorCode.CONFLICT.httpStatus());
        assertEquals(502, CommonErrorCode.UPSTREAM_FAILURE.httpStatus());
        assertEquals(500, CommonErrorCode.INTERNAL_ERROR.httpStatus());
    }

    @Test
    void coversTheStatusesServicesMapToday() {
        // The six RestExceptionHandler statuses (401/403/404/400/409/502) plus the
        // Ktor StatusPages trio (400/404/500) must all be expressible.
        Set<Integer> covered = new HashSet<>(Arrays.stream(CommonErrorCode.values())
                .map(CommonErrorCode::httpStatus)
                .toList());
        for (int expected : new int[]{400, 401, 403, 404, 409, 500, 502}) {
            assertTrue(covered.contains(expected), "no common code maps to " + expected);
        }
    }
}
