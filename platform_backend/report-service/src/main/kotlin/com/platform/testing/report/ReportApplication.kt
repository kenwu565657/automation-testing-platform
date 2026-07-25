package com.platform.testing.report

import com.platform.testing.report.application.LiveExecutionTracker
import com.platform.testing.report.application.ReportAggregationService
import com.platform.testing.report.application.TrendAnalysisService
import com.platform.testing.report.infrastructure.elasticsearch.ElasticsearchClient
import com.platform.testing.report.infrastructure.elasticsearch.ElasticsearchReportRepository
import com.platform.testing.report.infrastructure.elasticsearch.EsIndexManager
import com.platform.testing.report.infrastructure.kafka.KafkaConsumerService
import com.platform.testing.report.infrastructure.kafka.KafkaEventHandler
import com.platform.testing.report.infrastructure.redis.RedisStateReader
import com.platform.testing.report.interfaces.plugin.*
import io.ktor.server.application.*
import io.ktor.server.sse.*
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

fun main(args: Array<String>) = io.ktor.server.cio.EngineMain.main(args)

fun Application.module() {
    val log = LoggerFactory.getLogger("ReportApplication")

    // ---- Infrastructure ----

    val esHost   = environment.config.propertyOrNull("elasticsearch.host")?.getString() ?: "localhost"
    val esPort   = environment.config.propertyOrNull("elasticsearch.port")?.getString()?.toInt() ?: 9200
    val esScheme = environment.config.propertyOrNull("elasticsearch.scheme")?.getString() ?: "http"
    val esIndex  = environment.config.propertyOrNull("elasticsearch.indices.reports")?.getString() ?: "test-reports"

    val esClient          = ElasticsearchClient(esHost, esPort, esScheme)
    val reportRepository  = ElasticsearchReportRepository(esClient, esIndex)
    val indexManager       = EsIndexManager(esClient)

    val redisUrl    = environment.config.propertyOrNull("redis.url")?.getString() ?: "redis://localhost:6379"
    val redisReader = RedisStateReader(redisUrl)

    // ---- Application Services ----

    val reportService = ReportAggregationService(reportRepository)
    val trendService  = TrendAnalysisService(reportRepository)
    val liveTracker   = LiveExecutionTracker(redisReader)

    // ---- Ktor Plugins ----

    install(SSE)
    configureSerialization()
    configureStatusPages()
    configureMonitoring()
    configureRouting(reportService, trendService, liveTracker)

    // ---- Kafka Consumer (background coroutine) ----

    val kafkaServers = environment.config.propertyOrNull("kafka.bootstrap-servers")?.getString() ?: "localhost:9092"
    val kafkaGroupId = environment.config.propertyOrNull("kafka.group-id")?.getString() ?: "report-service"

    val kafkaTopics = listOf(
        environment.config.propertyOrNull("kafka.topics.execution-started")?.getString()   ?: "test-execution.started",
        environment.config.propertyOrNull("kafka.topics.step-completed")?.getString()       ?: "test-step.completed",
        environment.config.propertyOrNull("kafka.topics.execution-completed")?.getString()  ?: "test-execution.completed"
    )

    val eventHandler   = KafkaEventHandler(reportService)
    val kafkaConsumer  = KafkaConsumerService(kafkaServers, kafkaGroupId, eventHandler, kafkaTopics)

    launch {
        try {
            indexManager.ensureIndices()
            log.info("Elasticsearch indices ready")
        } catch (e: Exception) {
            log.warn("Could not ensure ES indices (ES may not be running): {}", e.message)
        }

        log.info("Starting Kafka consumer for topics: {}", kafkaTopics)
        kafkaConsumer.startConsuming()
    }

    environment.monitor.subscribe(ApplicationStopping) {
        log.info("Report Service shutting down")
        kafkaConsumer.stop()
        redisReader.close()
        esClient.close()
    }

    log.info("Report Service module loaded")
}
