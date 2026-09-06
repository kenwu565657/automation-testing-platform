package com.platform.testing.domain.testcase.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestCaseIdTest {

    @Test
    void ofAndGenerate() {
        TestCaseId generated = TestCaseId.generate();
        TestCaseId same = TestCaseId.of(generated.value());
        assertEquals(generated, same);
        assertNotEquals(generated, TestCaseId.generate());
    }

    @Test
    void rejectsBlank() {
        assertThrows(NullPointerException.class, () -> TestCaseId.of(null));
        assertThrows(IllegalArgumentException.class, () -> TestCaseId.of("  "));
    }
}
