package com.platform.testing.report.interfaces

import com.platform.testing.report.application.LiveExecutionTracker
import com.platform.testing.report.application.ReportAggregationService
import com.platform.testing.report.application.TrendAnalysisService
import com.platform.testing.report.domain.model.TestReport
import com.platform.testing.report.domain.repository.TestReportRepository
import com.platform.testing.report.infrastructure.redis.RedisStateReader
import com.platform.testing.report.interfaces.plugin.configureRouting
import com.platform.testing.report.interfaces.plugin.configureSerialization
import com.platform.testing.report.interfaces.plugin.configureStatusPages
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.sse.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains

class ReportRoutesTest {

    private val repository = mockk<TestReportRepository>()
    private val reportService = ReportAggregationService(repository)
    private val trendService = TrendAnalysisService(repository)
    private val redisReader = mockk<RedisStateReader>()
    private val liveTracker = LiveExecutionTracker(redisReader)

    private fun ApplicationTestBuilder.configureApp() {
        application {
            install(SSE)
            configureSerialization()
            configureStatusPages()
            configureRouting(reportService, trendService, liveTracker)
        }
    }

    @Test
    fun `GET info returns service info`() = testApplication {
        configureApp()

        val response = client.get("/info")

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), "report-service")
    }

    @Test
    fun `GET report by executionId returns 200`() = testApplication {
        configureApp()

        val report = TestReport(
            executionId = "exec-1",
            testCaseId = "tc-1",
            testCaseName = "Login Test",
            testType = "WEB_E2E",
            status = "PASSED",
            startedAt = "2026-03-22T10:00:00Z",
            durationMillis = 5000
        )
        coEvery { repository.findByExecutionId("exec-1") } returns report

        val response = client.get("/api/v1/reports/exec-1")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertContains(body, "exec-1")
        assertContains(body, "PASSED")
    }

    @Test
    fun `GET report by executionId returns 404 when not found`() = testApplication {
        configureApp()

        coEvery { repository.findByExecutionId("not-found") } returns null

        val response = client.get("/api/v1/reports/not-found")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET reports by testCaseId returns list`() = testApplication {
        configureApp()

        coEvery { repository.findByTestCaseId("tc-1", 10) } returns listOf(
            TestReport("exec-1", "tc-1", "Login", "WEB_E2E", status = "PASSED",
                startedAt = "2026-03-22T10:00:00Z", durationMillis = 5000),
            TestReport("exec-2", "tc-1", "Login", "WEB_E2E", status = "FAILED",
                startedAt = "2026-03-22T11:00:00Z", durationMillis = 3000)
        )

        val response = client.get("/api/v1/reports/test-case/tc-1?limit=10")

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), "exec-1")
        assertContains(response.bodyAsText(), "exec-2")
    }
}
