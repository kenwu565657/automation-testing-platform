package com.platform.testing.domain.environment;

import com.platform.testing.domain.environment.valueobject.EnvironmentId;
import com.platform.testing.domain.project.valueobject.ProjectId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentTest {

    @Test
    void variablesAndBaseUrl() {
        Environment env = Environment.create("staging", "https://stg.example", ProjectId.generate());
        env.setVariable("token", "abc");
        env.setVariable("token", "xyz");
        assertEquals("xyz", env.getVariables().get("token"));

        env.removeVariable("token");
        assertTrue(env.getVariables().isEmpty());

        env.updateBaseUrl("https://stg2.example");
        assertEquals("https://stg2.example", env.getBaseUrl());

        env.deactivate();
        assertFalse(env.isActive());
        assertThrows(UnsupportedOperationException.class, () -> env.getVariables().put("x", "y"));
    }

    @Test
    void reconstituteCopiesVariables() {
        Environment env = Environment.reconstitute(
                EnvironmentId.generate(), "prod", "https://prod.example",
                ProjectId.generate(), Map.of("k", "v"), true,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
        assertEquals("v", env.getVariables().get("k"));
    }
}
