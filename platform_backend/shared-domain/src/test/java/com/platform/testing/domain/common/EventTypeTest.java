package com.platform.testing.domain.common;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventTypeTest {

    @Test
    void namesAreUniqueAndDotted() throws IllegalAccessException {
        Set<String> values = new HashSet<>();
        for (Field field : EventType.class.getFields()) {
            String value = (String) field.get(null);
            assertTrue(values.add(value), "duplicate event type: " + value);
            assertTrue(value.startsWith("test."), value);
        }
        assertEquals(8, values.size());
    }
}
