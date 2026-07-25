package com.platform.testing.engine.infrastructure.verticle;

import com.platform.testing.engine.infrastructure.kafka.KafkaConsumerManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event-loop verticle — reads Kafka and forwards to worker verticles
 * via the Vert.x Event Bus. Does NO blocking work.
 */
public class ExecutionConsumerVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(ExecutionConsumerVerticle.class);
    static final String WORKER_ADDRESS = "engine.execute";

    private KafkaConsumerManager kafkaConsumer;

    @Override
    public void start(Promise<Void> startPromise) {
        var topic = config()
                .getJsonObject("kafka", new JsonObject())
                .getJsonObject("topics", new JsonObject())
                .getString("execution-requested", "test-execution.requested");

        kafkaConsumer = new KafkaConsumerManager(vertx, config(), topic);

        kafkaConsumer.start(payload -> {
            log.info("Dispatching execution: testCase={} device={}",
                    payload.getString("testCaseId"),
                    payload.getJsonObject("deviceProfile", new JsonObject()).getString("name", "default"));

            Promise<Void> p = Promise.promise();
            vertx.eventBus().<JsonObject>request(WORKER_ADDRESS, payload, ar -> {
                if (ar.succeeded()) {
                    log.info("Execution result: {}", ar.result().body());
                    p.complete();
                } else {
                    log.error("Execution failed", ar.cause());
                    p.fail(ar.cause());
                }
            });
            return p.future();
        }).onComplete(startPromise);
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        if (kafkaConsumer != null) {
            kafkaConsumer.stop().onComplete(stopPromise);
        } else {
            stopPromise.complete();
        }
    }
}
