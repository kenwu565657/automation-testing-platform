package com.valdifly.domain.testdefinition.valueobject;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataTableSpecTest {

    @Test
    void rejectsBlankNameOrEmptyRows() {
        assertThrows(IllegalArgumentException.class, () ->
                new DataTableSpec("  ", List.of(Map.of("user", "a"))));
        assertThrows(IllegalArgumentException.class, () ->
                new DataTableSpec("logins", List.of()));
    }

    @Test
    void rejectsMismatchedColumns() {
        assertThrows(IllegalArgumentException.class, () -> new DataTableSpec("logins", List.of(
                Map.of("user", "a", "password", "x"),
                Map.of("user", "b")
        )));
    }

    @Test
    void fromParametersAndRoundTrip() {
        DataTableSpec spec = new DataTableSpec("logins", List.of(
                Map.of("user", "alice", "password", "secret")
        ));
        assertEquals(List.of("user", "password"), spec.columns());
        assertEquals(spec.name(), DataTableSpec.require(spec.toParameters()).name());
        assertNull(DataTableSpec.from(List.of()));
        assertThrows(IllegalArgumentException.class, () -> DataTableSpec.require(List.of()));
    }
}
