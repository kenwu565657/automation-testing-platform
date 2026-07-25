package com.platform.testing.admin.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.testing.event.execution.TestExecutionRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private static final String TOPIC = "test-execution.requested";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishExecutionRequest(TestExecutionRequestEvent event) {
        try {
            var json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, event.testCaseId(), json)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish execution request for {}", event.testCaseId(), ex);
                        } else {
                            log.info("Published execution request: testCase={} topic={} offset={}",
                                    event.testCaseId(), TOPIC,
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to serialize execution request", e);
            throw new RuntimeException("Failed to publish execution request", e);
        }
    }
}
