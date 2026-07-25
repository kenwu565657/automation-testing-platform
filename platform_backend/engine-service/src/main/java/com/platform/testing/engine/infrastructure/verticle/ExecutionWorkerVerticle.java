package com.platform.testing.engine.infrastructure.verticle;

import com.platform.testing.engine.application.DriverFactoryRegistry;
import com.platform.testing.engine.application.EngineSelector;
import com.platform.testing.engine.application.ExecutionOrchestrator;
import com.platform.testing.engine.application.PageObjectResolver;
import com.platform.testing.engine.infrastructure.kafka.KafkaProducerManager;
import com.platform.testing.engine.infrastructure.redis.RedisExecutionStateStore;
import com.platform.testing.engine.infrastructure.redis.RedisPageObjectCache;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Worker verticle — runs on worker thread pool (blocking I/O allowed).
 * Handles Selenium/Appium interactions that block.
 */
public class ExecutionWorkerVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(ExecutionWorkerVerticle.class);

    private ExecutionOrchestrator orchestrator;
    private KafkaProducerManager kafkaProducer;

    @Override
    public void start(Promise<Void> startPromise) {
        // Redis
        var redisConn = config().getJsonObject("redis", new JsonObject())
                .getString("connection", "redis://localhost:6379");
        var redisClient = Redis.createClient(vertx, redisConn);
        var redisApi    = RedisAPI.api(redisClient);

        // Infrastructure
        var pageObjectCache = new RedisPageObjectCache(redisApi);
        var stateStore      = new RedisExecutionStateStore(redisApi);
        kafkaProducer       = new KafkaProducerManager(vertx, config());

        // Application
        var driverRegistry     = new DriverFactoryRegistry();
        var engineSelector     = new EngineSelector();
        var pageObjectResolver = new PageObjectResolver(pageObjectCache);

        orchestrator = new ExecutionOrchestrator(
                driverRegistry, engineSelector, pageObjectResolver, stateStore, kafkaProducer
        );

        // Listen on Event Bus
        vertx.eventBus().<JsonObject>consumer(
                ExecutionConsumerVerticle.WORKER_ADDRESS,
                message -> orchestrator.execute(message.body())
                        .onSuccess(result -> message.reply(result))
                        .onFailure(err -> {
                            log.error("Execution failed", err);
                            message.fail(500, err.getMessage());
                        })
        );

        log.info("ExecutionWorkerVerticle ready on thread: {}", Thread.currentThread().getName());
        startPromise.complete();
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        if (kafkaProducer != null) kafkaProducer.close();
        stopPromise.complete();
    }
}
