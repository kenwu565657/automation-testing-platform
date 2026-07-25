package com.platform.testing.report.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ExecutionSummary(
    val totalExecutions: Int,
    val passedCount: Int,
    val failedCount: Int,
    val abortedCount: Int,
    val overallPassRate: Double,
    val avgDurationMillis: Double,
    val flakyTests: List<FlakyTest> = emptyList()
)