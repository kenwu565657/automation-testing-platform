package com.valdifly.domain.organization.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrganizationIdTest {

    @Test
    void ofRejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> OrganizationId.of(" "));
    }

    @Test
    void ofPreservesValue() {
        assertEquals("org-1", OrganizationId.of("org-1").value());
    }
}
