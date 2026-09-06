package com.platform.testing.domain.projectmembership.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActionTest {

    @Test
    void iamNamesRoundTrip() {
        assertEquals("catalog:Write", Action.CATALOG_WRITE.iamName());
        assertEquals(Action.RUN_TRIGGER, Action.fromIamName("run:Trigger"));
        assertThrows(IllegalArgumentException.class, () -> Action.fromIamName("run:Explode"));
    }
}
