package com.platform.testing.report.domain.repository

import com.platform.testing.report.domain.model.*

/**
 * Port — repository interface. Implemented by ElasticsearchReportRepository.
 */
interface TestReportRepository {

    suspend fun save(report: TestReport)

    suspend fun findByExecutionId(executionId: String): TestReport?

    suspend fun findByTestCaseId(testCaseId: String, limit: Int = 20): List<TestReport>

    suspend fun search(query: ReportSearchQuery): ReportSearchResult

    suspend fun getSummary(timeRange: TimeRange): ExecutionSummary

    suspend fun getTrend(testCaseId: String, days: Int = 30): TrendData

    suspend fun getFlakyTests(minExecutions: Int = 5, maxPassRate: Double = 0.8): List<FlakyTest>
}

data class ReportSearchQuery(
    val testCaseName: String? = null,
    val testType: String? = null,
    val status: String? = null,
    val platform: String? = null,
    val tags: List<String> = emptyList(),
    val timeRange: TimeRange? = null,
    val page: Int = 0,
    val size: Int = 20
)

@kotlinx.serialization.Serializable
data class ReportSearchResult(
    val reports: List<TestReport>,
    val total: Long,
    val page: Int,
    val size: Int
)

data class TimeRange(
    val from: String,   // ISO-8601
    val to: String
)
