package com.platform.testing.report.application

import com.platform.testing.report.domain.model.FlakyTest
import com.platform.testing.report.domain.model.TrendData
import com.platform.testing.report.domain.model.TrendPoint
import com.platform.testing.report.domain.repository.TestReportRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TrendAnalysisServiceTest {

    private val repository = mockk<TestReportRepository>()
    private val service = TrendAnalysisService(repository)

    @Test
    fun `getTrend delegates to repository`() = runTest {
        val expected = TrendData(
            testCaseId = "tc-1",
            testCaseName = "Login Test",
            dataPoints = listOf(
                TrendPoint("2026-03-20", 10, 8, 2, 0.8, 3000.0),
                TrendPoint("2026-03-21", 12, 11, 1, 0.917, 2800.0)
            )
        )
        coEvery { repository.getTrend("tc-1", 30) } returns expected

        val result = service.getTrend("tc-1", 30)

        assertEquals(2, result.dataPoints.size)
        assertEquals("2026-03-20", result.dataPoints[0].date)
    }

    @Test
    fun `getFlakyTests returns tests below pass rate threshold`() = runTest {
        val expected = listOf(
            FlakyTest("tc-5", "Checkout Flow", 0.6, 10, listOf("PASSED", "FAILED", "PASSED", "FAILED"))
        )
        coEvery { repository.getFlakyTests(5, 0.8) } returns expected

        val result = service.getFlakyTests(5, 0.8)

        assertEquals(1, result.size)
        assertEquals("tc-5", result[0].testCaseId)
        assertEquals(0.6, result[0].passRate)
    }
}
