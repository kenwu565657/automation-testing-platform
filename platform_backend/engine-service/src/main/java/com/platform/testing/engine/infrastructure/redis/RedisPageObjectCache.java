package com.platform.testing.engine.infrastructure.redis;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.RedisAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Reads page-object locator data from Redis.
 *
 * Key format:   pageobject:{id}
 * Value format:  JSON with element names → { strategy, value }
 *
 * Example Redis value for key "pageobject:pg-login":
 * {
 *   "usernameInput": { "strategy": "CSS", "value": "#username" },
 *   "passwordInput": { "strategy": "CSS", "value": "#password" },
 *   "loginButton":   { "strategy": "ID",  "value": "btn-login" }
 * }
 */
public class RedisPageObjectCache {

    private static final Logger log = LoggerFactory.getLogger(RedisPageObjectCache.class);
    private static final String KEY_PREFIX = "pageobject:";

    private final RedisAPI redis;

    public RedisPageObjectCache(RedisAPI redis) {
        this.redis = redis;
    }

    public Future<JsonObject> get(String pageObjectId) {
        return redis.get(KEY_PREFIX + pageObjectId)
                .map(resp -> {
                    if (resp == null) {
                        log.warn("PageObject not found in cache: {}", pageObjectId);
                        return new JsonObject();
                    }
                    return new JsonObject(resp.toString());
                });
    }

    /**
     * Batch-fetch multiple page objects.
     * Returns { "pg-login": { "usernameInput": {...}, ... }, "pg-home": { ... } }
     */
    public Future<JsonObject> batchGet(Set<String> pageObjectIds) {
        if (pageObjectIds.isEmpty()) {
            return Future.succeededFuture(new JsonObject());
        }

        List<String> keys = new ArrayList<>();
        List<String> ids  = new ArrayList<>();
        for (var id : pageObjectIds) {
            keys.add(KEY_PREFIX + id);
            ids.add(id);
        }

        return redis.mget(keys)
                .map(resp -> {
                    var result = new JsonObject();
                    for (int i = 0; i < ids.size(); i++) {
                        var val = resp.get(i);
                        if (val != null && val.toString() != null) {
                            result.put(ids.get(i), new JsonObject(val.toString()));
                        } else {
                            log.warn("PageObject not in cache: {}", ids.get(i));
                            result.put(ids.get(i), new JsonObject());
                        }
                    }
                    return result;
                });
    }

    /**
     * Convenience: resolve all PAGE_OBJECT_REF targets in a featureFile.
     * Delegates to PageObjectResolver which calls batchGet.
     */
    public Future<JsonObject> resolveAll(JsonObject featureFile) {
        // Extract IDs inline — simple scan
        var ids = new java.util.HashSet<String>();
        var scenarios = featureFile.getJsonArray("scenarios", new io.vertx.core.json.JsonArray());
        for (int s = 0; s < scenarios.size(); s++) {
            var steps = scenarios.getJsonObject(s).getJsonArray("steps", new io.vertx.core.json.JsonArray());
            for (int i = 0; i < steps.size(); i++) {
                var action = steps.getJsonObject(i).getJsonObject("action");
                if (action == null) continue;
                var target = action.getJsonObject("target");
                if (target == null) continue;
                if ("PAGE_OBJECT_REF".equals(target.getString("locatorStrategy"))) {
                    var ref = target.getJsonObject("pageObjectRef");
                    if (ref != null && ref.getString("pageObjectId") != null) {
                        ids.add(ref.getString("pageObjectId"));
                    }
                }
            }
        }
        return batchGet(ids);
    }
}
