package com.valdifly.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.valdifly.infrastructure.error.CommonErrorCode;
import com.valdifly.infrastructure.json.ObjectMapperFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiResponseTest {

    record Sample(String name) {}

    @Test
    void okCarriesDataWithoutError() {
        ApiResponse<Sample> response = ApiResponse.ok(new Sample("admin"));

        assertTrue(response.success());
        assertEquals("admin", response.data().name());
        assertNull(response.error());
    }

    @Test
    void okWithoutData() {
        ApiResponse<Void> response = ApiResponse.ok();

        assertTrue(response.success());
        assertNull(response.data());
        assertNull(response.error());
    }

    @Test
    void failureCarriesErrorWithoutData() {
        ApiResponse<Sample> response = ApiResponse.failure(CommonErrorCode.NOT_FOUND, "nothing here");

        assertFalse(response.success());
        assertNull(response.data());
        assertEquals("NOT_FOUND", response.error().code());
    }

    @Test
    void serializesSuccessEnvelope() throws Exception {
        String json = ObjectMapperFactory.getInstance().writeValueAsString(ApiResponse.ok(new Sample("x")));
        JsonNode node = ObjectMapperFactory.getInstance().readTree(json);

        assertTrue(node.get("success").asBoolean());
        assertEquals("x", node.get("data").get("name").asText());
        assertFalse(node.has("error"), "error must be omitted on success, not null");
    }

    @Test
    void serializesFailureEnvelope() throws Exception {
        String json = ObjectMapperFactory.getInstance().writeValueAsString(
                ApiResponse.failure(CommonErrorCode.CONFLICT, "already running"));
        JsonNode node = ObjectMapperFactory.getInstance().readTree(json);

        assertFalse(node.get("success").asBoolean());
        assertFalse(node.has("data"), "data must be omitted on failure, not null");
        assertEquals("CONFLICT", node.get("error").get("code").asText());
    }
}
