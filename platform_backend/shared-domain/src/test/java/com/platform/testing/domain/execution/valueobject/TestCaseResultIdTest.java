package com.platform.testing.domain.execution.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestCaseResultIdTest {

    @Test
    void ofAndGenerate() {
        TestCaseResultId generated = TestCaseResultId.generate();
        assertEquals(generated, TestCaseResultId.of(generated.value()));
        assertNotEquals(generated, TestCaseResultId.generate());
    }

    @Test
    void rejectsBlank() {
        assertThrows(NullPointerException.class, () -> TestCaseResultId.of(null));
        assertThrows(IllegalArgumentException.class, () -> TestCaseResultId.of("  "));
    }
}
