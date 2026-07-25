package com.platform.testing.report.infrastructure.redis

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import org.slf4j.LoggerFactory

/**
 * Reads execution state from Redis (written by the Engine Service).
 * Uses Lettuce with Kotlin coroutines for non-blocking reads.
 */
class RedisStateReader(redisUrl: String = "redis://localhost:6379") {

    private val log = LoggerFactory.getLogger(javaClass)
    private val client: RedisClient = RedisClient.create(redisUrl)
    private val connection = client.connect()
    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    private val commands: RedisCoroutinesCommands<String, String> = connection.coroutines()

    companion object {
        private const val KEY_PREFIX = "execution:state:"
    }

    suspend fun getExecutionState(executionId: String): String? {
        return try {
            commands.get("$KEY_PREFIX$executionId")
        } catch (e: Exception) {
            log.warn("Failed to read execution state from Redis: {}", executionId, e)
            null
        }
    }

    fun close() {
        connection.close()
        client.shutdown()
    }
}
