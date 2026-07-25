package com.platform.testing.engine.application;

import com.platform.testing.engine.infrastructure.redis.RedisPageObjectCache;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Scans a featureFile JSON for PAGE_OBJECT_REF targets,
 * collects all referenced page-object IDs,
 * and batch-resolves them from Redis cache.
 */
public class PageObjectResolver {

    private final RedisPageObjectCache cache;

    public PageObjectResolver(RedisPageObjectCache cache) {
        this.cache = cache;
    }

    /**
     * Walk the featureFile JSON, extract all pageObjectIds,
     * fetch from Redis, return a context with "resolvedPageObjects".
     */
    public Future<JsonObject> resolve(JsonObject featureFile) {
        var ids = extractPageObjectIds(featureFile);
        if (ids.isEmpty()) {
            return Future.succeededFuture(new JsonObject()
                    .put("resolvedPageObjects", new JsonObject())
                    .put("variables", new JsonObject()));
        }
        return cache.batchGet(ids)
                .map(resolved -> new JsonObject()
                        .put("resolvedPageObjects", resolved)
                        .put("variables", new JsonObject()));
    }

    private Set<String> extractPageObjectIds(JsonObject featureFile) {
        var ids = new HashSet<String>();
        var scenarios = featureFile.getJsonArray("scenarios", new JsonArray());
        for (int s = 0; s < scenarios.size(); s++) {
            var steps = scenarios.getJsonObject(s).getJsonArray("steps", new JsonArray());
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
        return ids;
    }
}
