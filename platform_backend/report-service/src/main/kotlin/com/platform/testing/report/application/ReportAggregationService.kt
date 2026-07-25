package com.platform.testing.report.application

import com.platform.testing.report.domain.model.*
import com.platform.testing.report.domain.repository.ReportSearchQuery
import com.platform.testing.report.domain.repository.ReportSearchResult
import com.platform.testing.report.domain.repository.TestReportRepository
import com.platform.testing.report.domain.repository.TimeRange
import org.slf4j.LoggerFactory

/**
 * Application service — aggregates execution events into TestReport documents.
 * Called by KafkaEventHandler when an execution-completed event arrives.
 */
class ReportAggregationService(private val repository: TestReportRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Called when a test-execution.completed Kafka event arrives.
     * Builds a full TestReport and indexes it in ES.
     */
    suspend fun onExecutionCompleted(event: Map<String, Any?>) {
        val report = buildReport(event)
        repository.save(report)
        log.info(
            "Report indexed: execution={} test={} status={} device={}",
            report.executionId, report.testCaseName, report.status, report.deviceName
        )
    }

    suspend fun getReport(executionId: String): TestReport? {
        return repository.findByExecutionId(executionId)
    }

    suspend fun getReportsForTestCase(testCaseId: String, limit: Int = 20): List<TestReport> {
        return repository.findByTestCaseId(testCaseId, limit)
    }

    suspend fun search(query: ReportSearchQuery): ReportSearchResult {
        return repository.search(query)
    }

    suspend fun getSummary(from: String, to: String): ExecutionSummary {
        return repository.getSummary(TimeRange(from, to))
    }

    // ---- Private ----

    private fun buildReport(event: Map<String, Any?>): TestReport {
        val device = event["device"] as? Map<*, *>
        val stepResults = (event["stepResults"] as? List<*>)?.map { raw ->
            val step = raw as Map<*, *>
            StepResult(
                stepIndex = (step["stepIndex"] as? Number)?.toInt() ?: 0,
                stepText = step["stepText"] as? String ?: "",
                keyword = step["keyword"] as? String ?: "",
                status = step["status"] as? String ?: "SKIPPED",
                durationMillis = (step["durationMillis"] as? Number)?.toLong() ?: 0,
                errorMessage = step["errorMessage"] as? String,
                screenshotUrl = null,
                timestamp = step["timestamp"] as? String ?: ""
            )
        } ?: emptyList()

        val passedSteps = stepResults.count { it.status == "PASSED" }
        val totalSteps = stepResults.size
        val passRate = if (totalSteps > 0) passedSteps.toDouble() / totalSteps else 0.0
        val avgStepDuration = if (totalSteps > 0) {
            stepResults.map { it.durationMillis }.average()
        } else 0.0

        return TestReport(
            executionId = event["executionId"] as? String ?: "",
            testCaseId = event["testCaseId"] as? String ?: "",
            testCaseName = event["testCaseName"] as? String ?: "",
            testType = event["testType"] as? String ?: "",
            environmentId = event["environmentId"] as? String,
            status = event["status"] as? String ?: "UNKNOWN",
            startedAt = event["startedAt"] as? String ?: "",
            finishedAt = event["finishedAt"] as? String,
            durationMillis = (event["durationMillis"] as? Number)?.toLong() ?: 0,
            stepResults = stepResults,
            metrics = TestMetric(passRate = passRate, avgStepDuration = avgStepDuration),
            errorMessage = event["errorMessage"] as? String,
            deviceName = device?.get("name") as? String,
            platform = device?.get("platform") as? String,
            browser = device?.get("browser") as? String
        )
    }
}