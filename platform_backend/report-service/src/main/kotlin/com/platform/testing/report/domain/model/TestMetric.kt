package com.platform.testing.report.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TestMetric(
    val passRate: Double = 0.0,
    val avgStepDuration: Double = 0.0,
    // Load-test specific
    val throughput: Double? = null,
    val p50ResponseTime: Double? = null,
    val p95ResponseTime: Double? = null,
    val p99ResponseTime: Double? = null,
    val errorRate: Double? = null,
    val concurrentUsers: Int? = null
)