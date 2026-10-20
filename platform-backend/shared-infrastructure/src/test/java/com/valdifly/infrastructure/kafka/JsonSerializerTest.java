package com.valdifly.infrastructure.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.common.errors.SerializationException;

import static org.junit.jupiter.api.Assertions.*;

class JsonSerializerTest {

    private JsonSerializer<TestObject> serializer;

    @BeforeEach
    void setUp() {
        serializer = new JsonSerializer<>();
    }

    @Test
    void testSerializeSuccess() {
        TestObject obj = new TestObject("test-name", 42);
        
        byte[] result = serializer.serialize("test-topic", obj);
        
        assertNotNull(result);
        String jsonResult = new String(result, StandardCharsets.UTF_8);
        assertTrue(jsonResult.contains("\"name\":\"test-name\""));
        assertTrue(jsonResult.contains("\"value\":42"));
    }

    @Test
    void testSerializeNull() {
        byte[] result = serializer.serialize("test-topic", null);
        assertNull(result);
    }

    @Test
    void testSerializeException() {
        Object badObj = new Object() {
            public String getBadProperty() {
                throw new RuntimeException("Serialization failure!");
            }
        };

        JsonSerializer<Object> rawSerializer = new JsonSerializer<>();
        SerializationException e = assertThrows(SerializationException.class, () -> rawSerializer.serialize("test-topic", badObj));

        assertTrue(e.getMessage().contains("Error serializing message for topic 'test-topic'"));
    }

    static class TestObject {
        private String name;
        private int value;

        public TestObject() {} // Needed for JSON operations

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
