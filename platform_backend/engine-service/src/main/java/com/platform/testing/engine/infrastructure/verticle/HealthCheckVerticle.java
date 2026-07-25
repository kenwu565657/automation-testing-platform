package com.platform.testing.engine.infrastructure.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP verticle exposing /health and /health/ready endpoints.
 */
public class HealthCheckVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        var port = config().getJsonObject("server", new JsonObject()).getInteger("port", 8081);

        var router = Router.router(vertx);
        var hc     = HealthChecks.create(vertx);

        // Liveness — always up if the process is running
        hc.register("liveness", promise -> promise.complete(Status.OK()));

        // Readiness — check Redis connectivity
        var redisConn = config().getJsonObject("redis", new JsonObject())
                .getString("connection", "redis://localhost:6379");
        hc.register("redis", promise -> {
            var client = Redis.createClient(vertx, redisConn);
            RedisAPI.api(client).ping(java.util.List.of())
                    .onSuccess(r  -> { promise.complete(Status.OK());                      client.close(); })
                    .onFailure(err -> { promise.complete(Status.KO(new JsonObject().put("error", err.getMessage()))); client.close(); });
        });

        router.get("/health").handler(HealthCheckHandler.createWithHealthChecks(hc));
        router.get("/health/ready").handler(HealthCheckHandler.createWithHealthChecks(hc));

        // Simple info endpoint
        router.get("/info").handler(ctx ->
                ctx.json(new JsonObject()
                        .put("service", "engine-service")
                        .put("version", "0.1.0")
                        .put("status", "running")));

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port)
                .onSuccess(server -> {
                    log.info("Health check HTTP server on port {}", server.actualPort());
                    startPromise.complete();
                })
                .onFailure(startPromise::fail);
    }
}
