package com.platform.testing.report.infrastructure.kafka

import com.platform.testing.report.application.ReportAggregationService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory

/**
 * Processes Kafka events and delegates to application services.
 */
class KafkaEventHandler(
    private val reportService: ReportAggregationService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun handle(topic: String, value: String) {
        try {
            val jsonElement = Json.parseToJsonElement(value).jsonObject
            val eventType = jsonElement["eventType"]?.jsonPrimitive?.content

            when (eventType) {
                "EXECUTION_COMPLETED" -> {
                    log.info("Processing EXECUTION_COMPLETED: execution={}",
                        jsonElement["executionId"]?.jsonPrimitive?.content)

                    // Convert JsonElement to Map for the service
                    val map = toMap(jsonElement)
                    reportService.onExecutionCompleted(map)
                }
                "STEP_COMPLETED" -> {
                    // Step events can be used for real-time tracking
                    // For now we just log; the Report is built from the final event
                    log.debug("Step completed: execution={} step={}",
                        jsonElement["executionId"]?.jsonPrimitive?.content,
                        jsonElement["stepIndex"]?.jsonPrimitive?.content)
                }
                "EXECUTION_STARTED" -> {
                    log.debug("Execution started: execution={}",
                        jsonElement["executionId"]?.jsonPrimitive?.content)
                }
                else -> {
                    log.warn("Unknown event type: {}", eventType)
                }
            }
        } catch (e: Exception) {
            log.error("Failed to process Kafka event from topic {}", topic, e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun toMap(element: JsonElement): Map<String, Any?> {
        return when (element) {
            is JsonObject -> element.entries.associate { (k, v) -> k to toAny(v) }
            else -> emptyMap()
        }
    }

    private fun toAny(element: JsonElement): Any? {
        return when (element) {
            is JsonPrimitive -> {
                if (element.isString) element.content
                else element.content.toLongOrNull()
                    ?: element.content.toDoubleOrNull()
                    ?: element.content.toBooleanStrictOrNull()
            }
            is JsonArray -> element.map { toAny(it) }
            is JsonObject -> element.entries.associate { (k, v) -> k to toAny(v) }
            else -> null
        }
    }
}
