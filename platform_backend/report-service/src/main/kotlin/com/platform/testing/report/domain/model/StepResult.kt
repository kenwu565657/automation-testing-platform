package com.platform.testing.report.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StepResult(
    val stepIndex: Int,
    val stepText: String,
    val keyword: String,
    val status: String,            // PASSED | FAILED | SKIPPED
    val durationMillis: Long,
    val errorMessage: String? = null,
    val screenshotUrl: String? = null,
    val timestamp: String
)