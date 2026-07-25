package com.platform.testing.engine.infrastructure.kafka;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Manages a Vert.x Kafka consumer for a single topic.
 * Delegates each message to a handler function.
 */
public class KafkaConsumerManager {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerManager.class);

    private final KafkaConsumer<String, String> consumer;
    private final String topic;

    public KafkaConsumerManager(Vertx vertx, JsonObject config, String topic) {
        this.topic = topic;

        Map<String, String> kafkaConfig = new HashMap<>();
        kafkaConfig.put("bootstrap.servers", config.getString("kafka.bootstrap.servers", "localhost:9092"));
        kafkaConfig.put("group.id",          config.getString("kafka.group.id", "engine-service"));
        kafkaConfig.put("key.deserializer",   "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaConfig.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaConfig.put("auto.offset.reset",  "earliest");
        kafkaConfig.put("enable.auto.commit", "false");

        this.consumer = KafkaConsumer.create(vertx, kafkaConfig);
    }

    /**
     * Subscribe and start consuming.
     * @param handler receives each message value as JsonObject, returns a Future to signal completion.
     */
    public Future<Void> start(Function<JsonObject, Future<Void>> handler) {
        consumer.handler(record -> {
            log.debug("Kafka record: topic={} key={} partition={} offset={}",
                    record.topic(), record.key(), record.partition(), record.offset());

            var payload = new JsonObject(record.value());
            handler.apply(payload)
                    .onSuccess(v -> consumer.commit()
                            .onFailure(err -> log.warn("Offset commit failed", err)))
                    .onFailure(err -> log.error("Message handling failed", err));
        });

        consumer.exceptionHandler(err -> log.error("Kafka consumer error", err));

        return consumer.subscribe(topic)
                .onSuccess(v -> log.info("Subscribed to topic: {}", topic))
                .mapEmpty();
    }

    public Future<Void> stop() {
        return consumer.close();
    }
}
