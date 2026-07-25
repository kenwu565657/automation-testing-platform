package com.platform.testing.engine.infrastructure.kafka;

import com.platform.testing.engine.domain.device.DeviceProfile;
import com.platform.testing.domain.execution.StepExecutionResult;
import com.platform.testing.domain.execution.TestExecution;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class KafkaProducerManager {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerManager.class);

    private static final String TOPIC_STARTED   = "test-execution.started";
    private static final String TOPIC_STEP      = "test-step.completed";
    private static final String TOPIC_COMPLETED = "test-execution.completed";

    private final KafkaProducer<String, String> producer;

    public KafkaProducerManager(Vertx vertx, JsonObject config) {
        Map<String, String> cfg = new HashMap<>();
        cfg.put("bootstrap.servers", config.getString("kafka.bootstrap.servers", "localhost:9092"));
        cfg.put("key.serializer",    "org.apache.kafka.common.serialization.StringSerializer");
        cfg.put("value.serializer",  "org.apache.kafka.common.serialization.StringSerializer");
        cfg.put("acks",              "all");

        this.producer = KafkaProducer.create(vertx, cfg);
    }

    public void publishExecutionStarted(TestExecution exec, DeviceProfile device) {
        var event = exec.toJson()
                .put("eventType", "EXECUTION_STARTED")
                .put("device", device.toJson());
        send(TOPIC_STARTED, exec.getTestCaseId(), event);
    }

    public void publishStepCompleted(TestExecution exec, StepExecutionResult step) {
        var event = step.toJson()
                .put("eventType", "STEP_COMPLETED")
                .put("executionId", exec.getExecutionId())
                .put("testCaseId", exec.getTestCaseId());
        send(TOPIC_STEP, exec.getExecutionId(), event);
    }

    public void publishExecutionCompleted(TestExecution exec, DeviceProfile device) {
        var event = exec.toJson()
                .put("eventType", "EXECUTION_COMPLETED")
                .put("device", device.toJson());
        send(TOPIC_COMPLETED, exec.getTestCaseId(), event);
    }

    private void send(String topic, String key, JsonObject event) {
        var record = KafkaProducerRecord.create(topic, key, event.encode());
        producer.write(record)
                .onSuccess(m -> log.debug("Published to {} p={} o={}", m.getTopic(), m.getPartition(), m.getOffset()))
                .onFailure(err -> log.error("Failed to publish to {}", topic, err));
    }

    public void close() {
        producer.close();
    }
}
