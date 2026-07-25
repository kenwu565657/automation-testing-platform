package com.platform.testing.report.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class TestReportTest {

    @Test
    fun `step counts are calculated correctly`() {
        val report = TestReport(
            executionId = "exec-1",
            testCaseId = "tc-1",
            testCaseName = "Login Test",
            testType = "WEB_E2E",
            status = "FAILED",
            startedAt = "2026-03-22T10:00:00Z",
            durationMillis = 5000,
            stepResults = listOf(
                StepResult(0, "navigate", "GIVEN", "PASSED", 500, timestamp = "2026-03-22T10:00:00Z"),
                StepResult(1, "type user", "WHEN", "PASSED", 300, timestamp = "2026-03-22T10:00:01Z"),
                StepResult(2, "type pass", "AND", "PASSED", 200, timestamp = "2026-03-22T10:00:01Z"),
                StepResult(3, "click login", "AND", "FAILED", 100, "Element not found", timestamp = "2026-03-22T10:00:02Z"),
                StepResult(4, "see welcome", "THEN", "SKIPPED", 0, timestamp = "2026-03-22T10:00:02Z")
            )
        )

        assertEquals(3, report.passedSteps)
        assertEquals(1, report.failedSteps)
        assertEquals(1, report.skippedSteps)
        assertEquals(5, report.totalSteps)
    }

    @Test
    fun `empty report has zero step counts`() {
        val report = TestReport(
            executionId = "exec-2",
            testCaseId = "tc-2",
            testCaseName = "Empty Test",
            testType = "API",
            status = "PASSED",
            startedAt = "2026-03-22T10:00:00Z",
            durationMillis = 0
        )

        assertEquals(0, report.passedSteps)
        assertEquals(0, report.failedSteps)
        assertEquals(0, report.totalSteps)
    }

    @Test
    fun `device info is stored`() {
        val report = TestReport(
            executionId = "exec-3",
            testCaseId = "tc-1",
            testCaseName = "Login Test",
            testType = "MOBILE_E2E",
            status = "PASSED",
            startedAt = "2026-03-22T10:00:00Z",
            durationMillis = 8000,
            deviceName = "Pixel 7",
            platform = "ANDROID",
            browser = "CHROME"
        )

        assertEquals("Pixel 7", report.deviceName)
        assertEquals("ANDROID", report.platform)
    }
}
