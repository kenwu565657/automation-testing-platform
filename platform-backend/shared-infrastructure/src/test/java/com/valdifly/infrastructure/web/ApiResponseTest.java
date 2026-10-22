package com.valdifly.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.valdifly.infrastructure.error.CommonErrorCode;
import com.valdifly.infrastructure.json.ObjectMapperFactory;
import com.valdifly.infrastructure.logging.LogFields;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApiResponseTest {

    record Sample(String name) {}

    @Test
    void okCarriesDataWithSuccessCode() {
        ApiResponse<Sample> response = ApiResponse.ok(new Sample("admin"));

        assertEquals(0, response.code());
        assertEquals("success", response.message());
        assertEquals("admin", response.data().name());
    }

    @Test
    void okWithoutData() {
        ApiResponse<Void> response = ApiResponse.ok();

        assertEquals(0, response.code());
        assertNull(response.data());
    }

    @Test
    void failureCarriesCodeWithoutData() {
        ApiResponse<Sample> response = ApiResponse.failure(CommonErrorCode.NOT_FOUND, "nothing here");

        assertEquals(40401, response.code());
        assertEquals("nothing here", response.message());
        assertNull(response.data());
    }

    @Test
    void failureFallsBackToDefaultMessage() {
        ApiResponse<Sample> response = ApiResponse.failure(CommonErrorCode.NOT_FOUND, null);

        assertEquals("Not found", response.message());
    }

    @Test
    void traceIdComesFromMdcWhenPresent() {
        MDC.put(LogFields.TRACE_ID, "7f3a");
        try {
            assertEquals("7f3a", ApiResponse.ok().traceId());
            assertEquals("7f3a", ApiResponse.failure(CommonErrorCode.INTERNAL_ERROR, null).traceId());
        } finally {
            MDC.clear();
        }
    }

    @Test
    void serializesSuccessEnvelope() throws Exception {
        String json = ObjectMapperFactory.getInstance().writeValueAsString(ApiResponse.ok(new Sample("x")));
        JsonNode node = ObjectMapperFactory.getInstance().readTree(json);

        assertEquals(0, node.get("code").asInt());
        assertEquals("success", node.get("message").asText());
        assertEquals("x", node.get("data").get("name").asText());
        assertFalse(node.has("traceId"), "traceId must be omitted when no trace is on the MDC");
    }

    @Test
    void serializesFailureEnvelope() throws Exception {
        String json = ObjectMapperFactory.getInstance().writeValueAsString(
                ApiResponse.failure(CommonErrorCode.CONFLICT, "already running"));
        JsonNode node = ObjectMapperFactory.getInstance().readTree(json);

        assertEquals(40901, node.get("code").asInt());
        assertEquals("already running", node.get("message").asText());
        assertFalse(node.has("data"), "data must be omitted on failure, not null");
    }
}
