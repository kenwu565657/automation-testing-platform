package com.valdifly.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valdifly.infrastructure.json.ObjectMapperFactory;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic Kafka JSON Serializer using the shared ObjectMapper.
 *
 * Usage in Spring Boot (application.yml):
 *   spring.kafka.producer.value-serializer: com.valdifly.infrastructure.kafka.JsonSerializer
 *
 * Usage in Vert.x / Ktor (Kafka properties):
 *   props.put("value.serializer", "com.valdifly.infrastructure.kafka.JsonSerializer");
 *
 * @param <T> The type to serialize
 */
public class JsonSerializer<T> implements Serializer<T> {

    private static final Logger log = LoggerFactory.getLogger(JsonSerializer.class);
    private final ObjectMapper mapper = ObjectMapperFactory.getInstance();

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }
        try {
            return mapper.writeValueAsBytes(data);
        } catch (Exception e) {
            String msg = String.format("Error serializing message for topic '%s': %s", topic, e.getMessage());
            log.error(msg, e);
            throw new SerializationException(msg, e);
        }
    }
}
