package com.valdifly.domain.environment;

import com.valdifly.domain.environment.valueobject.EnvironmentId;
import com.valdifly.domain.project.valueobject.ProjectId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentTest {

    @Test
    void variables() {
        Environment env = Environment.create("staging", ProjectId.generate());
        env.setVariable("token", "abc");
        env.setVariable("token", "xyz");
        assertEquals("xyz", env.getVariables().get("token"));

        env.setVariable(" padded ", "kept");
        assertEquals("kept", env.getVariables().get("padded"));
        assertTrue(env.getVariables().containsKey("padded"));

        env.removeVariable("token");
        env.removeVariable("padded");
        assertTrue(env.getVariables().isEmpty());

        env.deactivate();
        assertFalse(env.isActive());
        env.activate();
        assertTrue(env.isActive());
        assertThrows(UnsupportedOperationException.class, () -> env.getVariables().put("x", "y"));
    }

    @Test
    void rejectsBlankVariableKey() {
        Environment env = Environment.create("staging", ProjectId.generate());
        assertThrows(NullPointerException.class, () -> env.setVariable(null, "x"));
        assertThrows(IllegalArgumentException.class, () -> env.setVariable("   ", "x"));
    }

    @Test
    void reconstituteCopiesVariables() {
        Environment env = Environment.reconstitute(
                EnvironmentId.generate(), "prod",
                ProjectId.generate(), Map.of("k", "v"), true,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
        assertEquals("v", env.getVariables().get("k"));
    }
}
