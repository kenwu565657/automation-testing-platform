package com.platform.testing.domain.common;

import com.platform.testing.domain.project.valueobject.ProjectId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValueObjectTest {

    @Test
    void valueIsAvailableThroughTheInterface() {
        ValueObject<String> id = ProjectId.of("proj-1");
        assertEquals("proj-1", id.value());
    }
}
