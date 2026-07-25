package com.platform.testing.report.infrastructure.elasticsearch

import org.slf4j.LoggerFactory

/**
 * Creates ES indices with proper mappings on startup.
 */
class EsIndexManager(private val client: ElasticsearchClient) {

    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun ensureIndices() {
        if (!client.indexExists("test-reports")) {
            client.createIndex("test-reports", REPORT_INDEX_MAPPINGS)
            log.info("Created index: test-reports")
        } else {
            log.info("Index already exists: test-reports")
        }
    }

    companion object {
        val REPORT_INDEX_MAPPINGS = """
        {
          "settings": {
            "number_of_shards": 2,
            "number_of_replicas": 1,
            "refresh_interval": "5s"
          },
          "mappings": {
            "properties": {
              "executionId":    { "type": "keyword" },
              "testCaseId":     { "type": "keyword" },
              "testCaseName":   { "type": "text", "fields": { "keyword": { "type": "keyword" } } },
              "testType":       { "type": "keyword" },
              "environmentId":  { "type": "keyword" },
              "status":         { "type": "keyword" },
              "startedAt":      { "type": "date" },
              "finishedAt":     { "type": "date" },
              "durationMillis": { "type": "long" },
              "errorMessage":   { "type": "text" },
              "deviceName":     { "type": "keyword" },
              "platform":       { "type": "keyword" },
              "browser":        { "type": "keyword" },
              "tags":           { "type": "keyword" },
              "passedSteps":    { "type": "integer" },
              "failedSteps":    { "type": "integer" },
              "skippedSteps":   { "type": "integer" },
              "totalSteps":     { "type": "integer" },
              "stepResults": {
                "type": "nested",
                "properties": {
                  "stepIndex":     { "type": "integer" },
                  "stepText":      { "type": "text" },
                  "keyword":       { "type": "keyword" },
                  "status":        { "type": "keyword" },
                  "durationMillis":{ "type": "long" },
                  "errorMessage":  { "type": "text" },
                  "screenshotUrl": { "type": "keyword" },
                  "timestamp":     { "type": "date" }
                }
              },
              "metrics": {
                "properties": {
                  "passRate":        { "type": "double" },
                  "avgStepDuration": { "type": "double" },
                  "throughput":      { "type": "double" },
                  "p50ResponseTime": { "type": "double" },
                  "p95ResponseTime": { "type": "double" },
                  "p99ResponseTime": { "type": "double" },
                  "errorRate":       { "type": "double" },
                  "concurrentUsers": { "type": "integer" }
                }
              }
            }
          }
        }
        """.trimIndent()
    }
}
