package com.platform.testing.report.application

import com.platform.testing.report.infrastructure.redis.RedisStateReader
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory

/**
 * Reads real-time execution state from Redis and exposes it as
 * a Kotlin Flow — perfect for SSE streaming to the frontend.
 */
class LiveExecutionTracker(private val redisReader: RedisStateReader) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Returns a Flow that emits the current execution state every [intervalMs].
     * The flow completes when the execution reaches a terminal status.
     */
    fun track(executionId: String, intervalMs: Long = 1000): Flow<String> = flow {
        var terminal = false
        while (!terminal) {
            val state = redisReader.getExecutionState(executionId)
            if (state != null) {
                emit(state)
                val status = extractStatus(state)
                terminal = status in setOf("PASSED", "FAILED", "ABORTED")
            } else {
                emit("""{"executionId":"$executionId","status":"NOT_FOUND"}""")
                terminal = true
            }
            if (!terminal) {
                delay(intervalMs)
            }
        }
    }

    private fun extractStatus(json: String): String {
        // Simple extraction without full JSON parsing
        val regex = """"status"\s*:\s*"(\w+)"""".toRegex()
        return regex.find(json)?.groupValues?.get(1) ?: "UNKNOWN"
    }
}