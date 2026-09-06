package com.platform.testing.domain.testdefinition.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestStepTest {

    @Test
    void nullActionParametersBecomeEmpty() {
        TestStep step = TestStep.create(
                1, "click", ActionType.CLICK, null, null,
                null, null, false, 0, 0
        );
        assertTrue(step.actionParameters().isEmpty());
        assertEquals(ActionType.CLICK, step.actionType());
    }

    @Test
    void requiresIdAndAction() {
        assertThrows(NullPointerException.class, () -> new TestStep(
                null, 1, "n", ActionType.CLICK, null, null, null, null, false, 0, 0
        ));
        assertThrows(IllegalArgumentException.class, () -> new TestStep(
                TestStepId.of("  "), 1, "n", ActionType.CLICK, null, null, null, null, false, 0, 0
        ));
        assertThrows(NullPointerException.class, () -> TestStep.create(
                1, "n", null, null, null, null, null, false, 0, 0
        ));
    }
}
