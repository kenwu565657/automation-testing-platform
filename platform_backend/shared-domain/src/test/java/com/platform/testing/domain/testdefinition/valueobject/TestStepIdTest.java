package com.platform.testing.domain.testdefinition.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestStepIdTest {

    @Test
    void ofAndGenerate() {
        TestStepId generated = TestStepId.generate();
        assertEquals(generated, TestStepId.of(generated.value()));
        assertNotEquals(generated, TestStepId.generate());
    }

    @Test
    void rejectsBlank() {
        assertThrows(NullPointerException.class, () -> TestStepId.of(null));
        assertThrows(IllegalArgumentException.class, () -> TestStepId.of("  "));
    }
}
