package com.valdifly.domain.visualbaseline.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VisualBaselineIdTest {

    @Test
    void ofRejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> VisualBaselineId.of(" "));
    }

    @Test
    void generateHasValue() {
        assertEquals(36, VisualBaselineId.generate().value().length());
    }
}
