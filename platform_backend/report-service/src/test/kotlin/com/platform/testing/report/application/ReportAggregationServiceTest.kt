package com.platform.testing.report.application

import com.platform.testing.report.domain.model.TestReport
import com.platform.testing.report.domain.repository.TestReportRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ReportAggregationServiceTest {

    private val repository = mockk<TestReportRepository>()
    private val service = ReportAggregationService(repository)

    @Test
    fun `onExecutionCompleted builds and saves report`() = runTest {
        coEvery { repository.save(any()) } just Runs

        val event = mapOf<String, Any?>(
            "executionId" to "exec-1",
            "testCaseId" to "tc-1",
            "testCaseName" to "Login Test",
            "testType" to "WEB_E2E",
            "status" to "PASSED",
            "startedAt" to "2026-03-22T10:00:00Z",
            "finishedAt" to "2026-03-22T10:00:05Z",
            "durationMillis" to 5000L,
            "stepResults" to listOf(
                mapOf(
                    "stepIndex" to 0,
                    "stepText" to "navigate to login",
                    "keyword" to "GIVEN",
                    "status" to "PASSED",
                    "durationMillis" to 500L,
                    "timestamp" to "2026-03-22T10:00:00Z"
                )
            ),
            "device" to mapOf(
                "name" to "Chrome Desktop",
                "platform" to "WEB",
                "browser" to "CHROME"
            )
        )

        service.onExecutionCompleted(event)

        coVerify(exactly = 1) { repository.save(match { report ->
            report.executionId == "exec-1" &&
                    report.status == "PASSED" &&
                    report.deviceName == "Chrome Desktop" &&
                    report.stepResults.size == 1
        }) }
    }

    @Test
    fun `getReport delegates to repository`() = runTest {
        val expected = TestReport(
            executionId = "exec-1",
            testCaseId = "tc-1",
            testCaseName = "Login Test",
            testType = "WEB_E2E",
            status = "PASSED",
            startedAt = "2026-03-22T10:00:00Z",
            durationMillis = 5000
        )
        coEvery { repository.findByExecutionId("exec-1") } returns expected

        val result = service.getReport("exec-1")

        assertNotNull(result)
        assertEquals("exec-1", result.executionId)
    }

    @Test
    fun `getReport returns null for missing execution`() = runTest {
        coEvery { repository.findByExecutionId("not-found") } returns null

        val result = service.getReport("not-found")

        assertEquals(null, result)
    }
}
