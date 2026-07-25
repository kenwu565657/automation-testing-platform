package com.platform.testing.admin.infrastructure.kafka;

import com.platform.testing.event.common.DomainEventEnvelope;
import com.platform.testing.event.execution.TestExecutionRequestEvent;
import com.platform.testing.event.topic.KafkaTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ExecutionRequestProducer {

    private static final Logger log = LoggerFactory.getLogger(ExecutionRequestProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ExecutionRequestProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(TestExecutionRequestEvent event) {
        var envelope = DomainEventEnvelope.wrap(
                event, "TestExecutionRequest", "admin-service", event.runId()
        );

        kafkaTemplate.send(
                KafkaTopic.TEST_EXECUTION_REQUEST,
                event.executionTargetId(),  // partition key
                envelope
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send execution request for run {}", event.runId(), ex);
            } else {
                log.info("Sent execution request: runId={}, topic={}, partition={}",
                        event.runId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition());
            }
        });
    }
}
