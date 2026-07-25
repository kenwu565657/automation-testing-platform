package com.platform.testing.engine.infrastructure.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.ThreadingModel;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Root verticle — loads config, then deploys child verticles.
 */
public class MainVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        loadConfig()
                .compose(this::deployVerticles)
                .onSuccess(v -> {
                    log.info("=== Engine Service started ===");
                    startPromise.complete();
                })
                .onFailure(err -> {
                    log.error("Engine Service failed to start", err);
                    startPromise.fail(err);
                });
    }

    private Future<JsonObject> loadConfig() {
        var yamlStore = new ConfigStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setConfig(new JsonObject().put("path", "config.yaml"));

        var envStore = new ConfigStoreOptions().setType("env");

        var options = new ConfigRetrieverOptions()
                .addStore(yamlStore)
                .addStore(envStore);    // env vars override file

        return ConfigRetriever.create(vertx, options).getConfig();
    }

    private Future<Void> deployVerticles(JsonObject config) {
        // 1. Health check HTTP (event loop)
        var healthFuture = vertx.deployVerticle(
                new HealthCheckVerticle(),
                new DeploymentOptions().setConfig(config)
        );

        // 2. Kafka consumer (event loop)
        var consumerFuture = vertx.deployVerticle(
                new ExecutionConsumerVerticle(),
                new DeploymentOptions().setConfig(config)
        );

        // 3. Workers that run actual tests (blocking I/O)
        var workerCount = config.getJsonObject("engine", new JsonObject())
                .getInteger("worker-pool-size", Runtime.getRuntime().availableProcessors());

        var workerFuture = vertx.deployVerticle(
                ExecutionWorkerVerticle::new,
                new DeploymentOptions()
                        .setConfig(config)
                        .setThreadingModel(ThreadingModel.WORKER)
                        .setInstances(workerCount)
        );

        return Future.all(healthFuture, consumerFuture, workerFuture)
                .onSuccess(v -> log.info("Deployed: health, consumer, {} workers", workerCount))
                .mapEmpty();
    }
}
