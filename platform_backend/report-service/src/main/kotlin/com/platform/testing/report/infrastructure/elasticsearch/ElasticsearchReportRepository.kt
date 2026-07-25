package com.platform.testing.report.infrastructure.elasticsearch

import com.platform.testing.report.domain.model.*
import com.platform.testing.report.domain.repository.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory

/**
 * Adapter — implements TestReportRepository using Elasticsearch.
 */
class ElasticsearchReportRepository(
    private val client: ElasticsearchClient,
    private val indexName: String = "test-reports"
) : TestReportRepository {

    private val log = LoggerFactory.getLogger(javaClass)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // ---- Write ----

    override suspend fun save(report: TestReport) {
        val doc = json.encodeToString(TestReport.serializer(), report)
        val resp = client.index(indexName, report.executionId, doc)
        log.debug("Indexed report {}: status={}", report.executionId, resp.status)
    }

    // ---- Read: single ----

    override suspend fun findByExecutionId(executionId: String): TestReport? {
        val resp = client.get(indexName, executionId)
        if (resp.status.value == 404) return null
        val body = Json.parseToJsonElement(resp.bodyAsText()).jsonObject
        val source = body["_source"] ?: return null
        return json.decodeFromJsonElement(TestReport.serializer(), source)
    }

    // ---- Read: by test case ----

    override suspend fun findByTestCaseId(testCaseId: String, limit: Int): List<TestReport> {
        val query = """
        {
          "size": $limit,
          "sort": [{ "startedAt": { "order": "desc" } }],
          "query": {
            "term": { "testCaseId": "$testCaseId" }
          }
        }
        """.trimIndent()
        return executeSearch(query)
    }

    // ---- Read: search ----

    override suspend fun search(query: ReportSearchQuery): ReportSearchResult {
        val filters = buildList {
            query.testCaseName?.let {
                add("""{ "match": { "testCaseName": "$it" } }""")
            }
            query.testType?.let {
                add("""{ "term": { "testType": "$it" } }""")
            }
            query.status?.let {
                add("""{ "term": { "status": "$it" } }""")
            }
            query.platform?.let {
                add("""{ "term": { "platform": "$it" } }""")
            }
            if (query.tags.isNotEmpty()) {
                add("""{ "terms": { "tags": ${query.tags.joinToString(",", "[\"", "\"]") } } }""")
            }
            query.timeRange?.let {
                add("""{ "range": { "startedAt": { "gte": "${it.from}", "lte": "${it.to}" } } }""")
            }
        }

        val filterClause = if (filters.isEmpty()) {
            """{ "match_all": {} }"""
        } else {
            """{ "bool": { "must": [${filters.joinToString(",")}] } }"""
        }

        val esQuery = """
        {
          "from": ${query.page * query.size},
          "size": ${query.size},
          "sort": [{ "startedAt": { "order": "desc" } }],
          "query": $filterClause
        }
        """.trimIndent()

        val resp = client.search(indexName, esQuery)
        val body = Json.parseToJsonElement(resp.bodyAsText()).jsonObject
        val hits = body["hits"]?.jsonObject
        val total = hits?.get("total")?.jsonObject?.get("value")?.jsonPrimitive?.long ?: 0
        val reports = hits?.get("hits")?.jsonArray?.mapNotNull { hit ->
            val source = hit.jsonObject["_source"] ?: return@mapNotNull null
            json.decodeFromJsonElement(TestReport.serializer(), source)
        } ?: emptyList()

        return ReportSearchResult(reports, total, query.page, query.size)
    }

    // ---- Read: summary ----

    override suspend fun getSummary(timeRange: TimeRange): ExecutionSummary {
        val query = """
        {
          "size": 0,
          "query": {
            "range": { "startedAt": { "gte": "${timeRange.from}", "lte": "${timeRange.to}" } }
          },
          "aggs": {
            "status_counts": {
              "terms": { "field": "status" }
            },
            "avg_duration": {
              "avg": { "field": "durationMillis" }
            }
          }
        }
        """.trimIndent()

        val resp = client.search(indexName, query)
        val body = Json.parseToJsonElement(resp.bodyAsText()).jsonObject
        val aggs = body["aggregations"]?.jsonObject
        val totalHits = body["hits"]?.jsonObject
            ?.get("total")?.jsonObject
            ?.get("value")?.jsonPrimitive?.int ?: 0

        val buckets = aggs?.get("status_counts")?.jsonObject
            ?.get("buckets")?.jsonArray ?: JsonArray(emptyList())

        var passed = 0; var failed = 0; var aborted = 0
        for (bucket in buckets) {
            val key = bucket.jsonObject["key"]?.jsonPrimitive?.content ?: continue
            val count = bucket.jsonObject["doc_count"]?.jsonPrimitive?.int ?: 0
            when (key) {
                "PASSED" -> passed = count
                "FAILED" -> failed = count
                "ABORTED" -> aborted = count
            }
        }

        val avgDuration = aggs?.get("avg_duration")?.jsonObject
            ?.get("value")?.jsonPrimitive?.double ?: 0.0
        val passRate = if (totalHits > 0) passed.toDouble() / totalHits else 0.0

        return ExecutionSummary(
            totalExecutions = totalHits,
            passedCount = passed,
            failedCount = failed,
            abortedCount = aborted,
            overallPassRate = passRate,
            avgDurationMillis = avgDuration
        )
    }

    // ---- Read: trend ----

    override suspend fun getTrend(testCaseId: String, days: Int): TrendData {
        val query = """
        {
          "size": 0,
          "query": {
            "bool": {
              "must": [
                { "term": { "testCaseId": "$testCaseId" } },
                { "range": { "startedAt": { "gte": "now-${days}d/d" } } }
              ]
            }
          },
          "aggs": {
            "daily": {
              "date_histogram": {
                "field": "startedAt",
                "calendar_interval": "day",
                "format": "yyyy-MM-dd"
              },
              "aggs": {
                "status_counts": { "terms": { "field": "status" } },
                "avg_duration":  { "avg": { "field": "durationMillis" } }
              }
            }
          }
        }
        """.trimIndent()

        val resp = client.search(indexName, query)
        val body = Json.parseToJsonElement(resp.bodyAsText()).jsonObject
        val buckets = body["aggregations"]?.jsonObject
            ?.get("daily")?.jsonObject
            ?.get("buckets")?.jsonArray ?: JsonArray(emptyList())

        val dataPoints = buckets.map { bucket ->
            val obj = bucket.jsonObject
            val date = obj["key_as_string"]?.jsonPrimitive?.content ?: ""
            val total = obj["doc_count"]?.jsonPrimitive?.int ?: 0

            val statusBuckets = obj["status_counts"]?.jsonObject
                ?.get("buckets")?.jsonArray ?: JsonArray(emptyList())
            var pass = 0; var fail = 0
            for (sb in statusBuckets) {
                when (sb.jsonObject["key"]?.jsonPrimitive?.content) {
                    "PASSED" -> pass = sb.jsonObject["doc_count"]?.jsonPrimitive?.int ?: 0
                    "FAILED" -> fail = sb.jsonObject["doc_count"]?.jsonPrimitive?.int ?: 0
                }
            }

            val avgDur = obj["avg_duration"]?.jsonObject
                ?.get("value")?.jsonPrimitive?.double ?: 0.0

            TrendPoint(
                date = date,
                totalExecutions = total,
                passCount = pass,
                failCount = fail,
                passRate = if (total > 0) pass.toDouble() / total else 0.0,
                avgDurationMillis = avgDur
            )
        }

        return TrendData(testCaseId = testCaseId, testCaseName = "", dataPoints = dataPoints)
    }

    // ---- Read: flaky tests ----

    override suspend fun getFlakyTests(minExecutions: Int, maxPassRate: Double): List<FlakyTest> {
        val query = """
        {
          "size": 0,
          "query": {
            "range": { "startedAt": { "gte": "now-30d/d" } }
          },
          "aggs": {
            "by_test_case": {
              "terms": { "field": "testCaseId", "size": 100 },
              "aggs": {
                "status_counts": { "terms": { "field": "status" } },
                "test_name": { "terms": { "field": "testCaseName.keyword", "size": 1 } },
                "recent_statuses": {
                  "top_hits": {
                    "size": 20,
                    "sort": [{ "startedAt": { "order": "desc" } }],
                    "_source": ["status"]
                  }
                }
              }
            }
          }
        }
        """.trimIndent()

        val resp = client.search(indexName, query)
        val body = Json.parseToJsonElement(resp.bodyAsText()).jsonObject
        val buckets = body["aggregations"]?.jsonObject
            ?.get("by_test_case")?.jsonObject
            ?.get("buckets")?.jsonArray ?: JsonArray(emptyList())

        return buckets.mapNotNull { bucket ->
            val obj = bucket.jsonObject
            val testCaseId = obj["key"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val execCount = obj["doc_count"]?.jsonPrimitive?.int ?: 0
            if (execCount < minExecutions) return@mapNotNull null

            val statusBuckets = obj["status_counts"]?.jsonObject
                ?.get("buckets")?.jsonArray ?: JsonArray(emptyList())
            val passCount = statusBuckets.firstOrNull {
                it.jsonObject["key"]?.jsonPrimitive?.content == "PASSED"
            }?.jsonObject?.get("doc_count")?.jsonPrimitive?.int ?: 0

            val passRate = passCount.toDouble() / execCount
            if (passRate > maxPassRate) return@mapNotNull null

            val testName = obj["test_name"]?.jsonObject
                ?.get("buckets")?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("key")?.jsonPrimitive?.content ?: ""

            val recentHits = obj["recent_statuses"]?.jsonObject
                ?.get("hits")?.jsonObject
                ?.get("hits")?.jsonArray ?: JsonArray(emptyList())
            val statusHistory = recentHits.map { hit ->
                hit.jsonObject["_source"]?.jsonObject
                    ?.get("status")?.jsonPrimitive?.content ?: "UNKNOWN"
            }

            FlakyTest(
                testCaseId = testCaseId,
                testCaseName = testName,
                passRate = passRate,
                executionCount = execCount,
                statusHistory = statusHistory
            )
        }
    }

    // ---- Helper ----

    private suspend fun executeSearch(query: String): List<TestReport> {
        val resp = client.search(indexName, query)
        val body = Json.parseToJsonElement(resp.bodyAsText()).jsonObject
        return body["hits"]?.jsonObject?.get("hits")?.jsonArray?.mapNotNull { hit ->
            val source = hit.jsonObject["_source"] ?: return@mapNotNull null
            json.decodeFromJsonElement(TestReport.serializer(), source)
        } ?: emptyList()
    }
}
