package com.platform.testing.report.application

import com.platform.testing.report.domain.model.FlakyTest
import com.platform.testing.report.domain.model.TrendData
import com.platform.testing.report.domain.repository.TestReportRepository
import org.slf4j.LoggerFactory

/**
 * Application service — trend analysis and flaky test detection.
 */
class TrendAnalysisService(private val repository: TestReportRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun getTrend(testCaseId: String, days: Int = 30): TrendData {
        return repository.getTrend(testCaseId, days)
    }

    suspend fun getFlakyTests(minExecutions: Int = 5, maxPassRate: Double = 0.8): List<FlakyTest> {
        val flakyTests = repository.getFlakyTests(minExecutions, maxPassRate)
        log.info("Found {} flaky tests (minExec={}, maxPassRate={})", flakyTests.size, minExecutions, maxPassRate)
        return flakyTests
    }
}