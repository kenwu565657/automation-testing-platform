package com.platform.testing.domain.pageobject.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageElementIdTest {

    @Test
    void ofAndGenerate() {
        PageElementId generated = PageElementId.generate();
        assertEquals(generated, PageElementId.of(generated.value()));
        assertNotEquals(generated, PageElementId.generate());
    }

    @Test
    void rejectsBlank() {
        assertThrows(NullPointerException.class, () -> PageElementId.of(null));
        assertThrows(IllegalArgumentException.class, () -> PageElementId.of("  "));
    }
}
