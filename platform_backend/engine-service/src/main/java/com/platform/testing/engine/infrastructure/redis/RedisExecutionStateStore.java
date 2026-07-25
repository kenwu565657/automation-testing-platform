package com.platform.testing.engine.infrastructure.redis;

import com.platform.testing.domain.execution.TestExecution;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.RedisAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persists real-time execution state to Redis so the frontend
 * can poll (or receive via SSE) live progress updates.
 */
public class RedisExecutionStateStore {

    private static final Logger log = LoggerFactory.getLogger(RedisExecutionStateStore.class);
    private static final String KEY_PREFIX  = "execution:state:";
    private static final String TTL_SECONDS = "86400";

    private final RedisAPI redis;

    public RedisExecutionStateStore(RedisAPI redis) {
        this.redis = redis;
    }

    public void saveState(TestExecution execution) {
        var key   = KEY_PREFIX + execution.getExecutionId();
        var value = execution.toJson().encode();

        redis.setex(key, TTL_SECONDS, value)
                .onFailure(err -> log.warn("Redis saveState failed for {}", execution.getExecutionId(), err));
    }

    public Future<JsonObject> getState(String executionId) {
        return redis.get(KEY_PREFIX + executionId)
                .map(resp -> resp != null ? new JsonObject(resp.toString()) : null);
    }

    public Future<Void> deleteState(String executionId) {
        return redis.del(java.util.List.of(KEY_PREFIX + executionId)).mapEmpty();
    }
}
