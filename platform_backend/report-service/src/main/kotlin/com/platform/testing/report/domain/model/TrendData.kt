package com.platform.testing.report.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TrendData(
    val testCaseId: String,
    val testCaseName: String,
    val dataPoints: List<TrendPoint>
)

@Serializable
data class TrendPoint(
    val date: String,
    val totalExecutions: Int,
    val passCount: Int,
    val failCount: Int,
    val passRate: Double,
    val avgDurationMillis: Double
)
