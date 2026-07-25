package com.platform.testing.report.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FlakyTest(
    val testCaseId: String,
    val testCaseName: String,
    val passRate: Double,
    val executionCount: Int,
    val statusHistory: List<String>   // ["PASSED","FAILED","PASSED","FAILED"]
)
