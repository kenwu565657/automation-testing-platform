package com.valdifly.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.valdifly.infrastructure.json.ObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaginatedDataTest {

    @Test
    void offsetModeComputesTotalPagesAndHasNext() {
        PaginatedData<String> page = PaginatedData.of(List.of("a", "b"), 0, 2, 5);

        assertEquals(0, page.pagination().page());
        assertEquals(2, page.pagination().size());
        assertEquals(5, page.pagination().total());
        assertEquals(3, page.pagination().totalPages());
        assertTrue(page.pagination().hasNext());
    }

    @Test
    void offsetModeLastPageHasNoNext() {
        PaginatedData<String> page = PaginatedData.of(List.of("e"), 2, 2, 5);

        assertFalse(page.pagination().hasNext());
    }

    @Test
    void offsetModeEmptyResult() {
        PaginatedData<String> page = PaginatedData.of(List.of(), 0, 20, 0);

        assertEquals(0, page.pagination().totalPages());
        assertFalse(page.pagination().hasNext());
    }

    @Test
    void cursorModeOmitsOffsetFields() throws Exception {
        PaginatedData<String> page = PaginatedData.ofCursor(List.of("a"), 50, true, "cursor-abc");
        JsonNode pagination = ObjectMapperFactory.getInstance()
                .readTree(ObjectMapperFactory.getInstance().writeValueAsString(page))
                .get("pagination");

        assertEquals(50, pagination.get("size").asInt());
        assertTrue(pagination.get("hasNext").asBoolean());
        assertEquals("cursor-abc", pagination.get("nextCursor").asText());
        assertFalse(pagination.has("page"), "cursor mode must not emit page");
        assertFalse(pagination.has("total"), "cursor mode must not emit total");
        assertFalse(pagination.has("totalPages"), "cursor mode must not emit totalPages");
    }

    @Test
    void offsetModeOmitsNextCursor() throws Exception {
        PaginatedData<String> page = PaginatedData.of(List.of("a"), 0, 1, 2);
        JsonNode pagination = ObjectMapperFactory.getInstance()
                .readTree(ObjectMapperFactory.getInstance().writeValueAsString(page))
                .get("pagination");

        assertFalse(pagination.has("nextCursor"), "offset mode must not emit nextCursor");
    }

    @Test
    void cursorModeWithoutFurtherPage() {
        PaginatedData<String> page = PaginatedData.ofCursor(List.of("a"), 50, false, null);

        assertFalse(page.pagination().hasNext());
        assertNull(page.pagination().nextCursor());
    }
}
