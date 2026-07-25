package com.platform.testing.report.domain.model

import kotlinx.serialization.Serializable

/**
 * Aggregate Root — final report of one test execution.
 * Stored as a document in Elasticsearch.
 */
@Serializable
data class TestReport(
    val executionId: String,
    val testCaseId: String,
    val testCaseName: String,
    val testType: String,
    val environmentId: String? = null,
    val status: String,
    val startedAt: String,
    val finishedAt: String? = null,
    val durationMillis: Long,
    val stepResults: List<StepResult> = emptyList(),
    val metrics: TestMetric? = null,
    val tags: List<String> = emptyList(),
    val errorMessage: String? = null,
    // Device info (denormalized for ES aggregation)
    val deviceName: String? = null,
    val platform: String? = null,
    val browser: String? = null
) {
    val passedSteps: Int get() = stepResults.count { it.status == "PASSED" }
    val failedSteps: Int get() = stepResults.count { it.status == "FAILED" }
    val skippedSteps: Int get() = stepResults.count { it.status == "SKIPPED" }
    val totalSteps: Int get() = stepResults.size
}