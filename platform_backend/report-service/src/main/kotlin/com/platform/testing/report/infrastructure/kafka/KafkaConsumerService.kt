package com.platform.testing.report.infrastructure.kafka

import kotlinx.coroutines.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.Properties

/**
 * Coroutine-based Kafka consumer.
 * Runs an infinite poll loop in a background coroutine.
 */
class KafkaConsumerService(
    private val bootstrapServers: String,
    private val groupId: String,
    private val eventHandler: KafkaEventHandler,
    private val topics: List<String> = listOf(
        "test-execution.started",
        "test-step.completed",
        "test-execution.completed"
    )
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Volatile
    private var running = true

    /**
     * Starts consuming in the current coroutine scope.
     * This suspends forever (until cancelled or stop() is called).
     */
    suspend fun startConsuming() = withContext(Dispatchers.IO) {
        val props = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
            put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "50")
        }

        val consumer = KafkaConsumer<String, String>(props)
        consumer.subscribe(topics)
        log.info("Kafka consumer subscribed to topics: {}", topics)

        try {
            while (running && isActive) {
                val records = consumer.poll(Duration.ofMillis(500))
                for (record in records) {
                    try {
                        eventHandler.handle(record.topic(), record.value())
                    } catch (e: Exception) {
                        log.error("Error processing record: topic={} offset={}",
                            record.topic(), record.offset(), e)
                    }
                }
                if (!records.isEmpty) {
                    consumer.commitSync()
                }
            }
        } finally {
            log.info("Kafka consumer shutting down")
            consumer.close()
        }
    }

    fun stop() {
        running = false
    }
}
