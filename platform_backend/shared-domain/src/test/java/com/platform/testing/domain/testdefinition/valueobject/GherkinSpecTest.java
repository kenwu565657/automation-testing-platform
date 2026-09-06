package com.platform.testing.domain.testdefinition.valueobject;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GherkinSpecTest {

    @Test
    void requiresFeatureAndScenario() {
        assertThrows(IllegalArgumentException.class, () ->
                new GherkinSpec("  ", "S", List.of()));
        assertThrows(IllegalArgumentException.class, () ->
                new GherkinSpec("F", "  ", List.of()));
        assertThrows(NullPointerException.class, () ->
                new GherkinLine(1, null, "text", false));
        assertThrows(IllegalArgumentException.class, () ->
                new GherkinLine(1, GherkinKeyword.THEN, "  ", false));
    }

    @Test
    void nullLinesBecomeEmpty() {
        GherkinSpec spec = new GherkinSpec("Auth", "Login", null);
        assertTrue(spec.lines().isEmpty());
        assertEquals("Auth", spec.featureTitle());
    }
}
