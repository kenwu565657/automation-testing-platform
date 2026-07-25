package com.platform.testing.engine.application;

import com.platform.testing.engine.domain.device.DeviceProfile;
import com.platform.testing.engine.domain.driver.ManagedDriver;
import com.platform.testing.engine.domain.engine.TestEngine;
import com.platform.testing.engine.domain.execution.StepStatus;
import com.platform.testing.domain.execution.TestExecution;
import com.platform.testing.engine.infrastructure.kafka.KafkaProducerManager;
import com.platform.testing.engine.infrastructure.redis.RedisExecutionStateStore;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ExecutionOrchestrator.class);

    private final DriverFactoryRegistry driverRegistry;
    private final EngineSelector engineSelector;
    private final PageObjectResolver pageObjectResolver;
    private final RedisExecutionStateStore stateStore;
    private final KafkaProducerManager kafka;

    public ExecutionOrchestrator(
            DriverFactoryRegistry driverRegistry,
            EngineSelector engineSelector,
            PageObjectResolver pageObjectResolver,
            RedisExecutionStateStore stateStore,
            KafkaProducerManager kafka
    ) {
        this.driverRegistry      = driverRegistry;
        this.engineSelector      = engineSelector;
        this.pageObjectResolver  = pageObjectResolver;
        this.stateStore          = stateStore;
        this.kafka               = kafka;
    }

    public Future<JsonObject> execute(JsonObject request) {
        var testCaseId   = request.getString("testCaseId");
        var testCaseName = request.getString("testCaseName");
        var testType     = request.getString("testType");
        var envId        = request.getString("environmentId");
        var featureFile  = request.getJsonObject("featureFile");
        var device       = DeviceProfile.fromJson(request.getJsonObject("deviceProfile"));

        var execution = new TestExecution(testCaseId, testCaseName, testType, envId);
        var engine    = engineSelector.select(testType);

        log.info("Execution {} starting: test={} device={} ({})",
                execution.getExecutionId(), testCaseName, device.name(), device.platform());

        return driverRegistry.createDriver(device)
                .compose(driver -> {
                    execution.start();
                    stateStore.saveState(execution);
                    kafka.publishExecutionStarted(execution, device);

                    return pageObjectResolver.resolve(featureFile)
                            .compose(ctx -> mergeEnvironmentVars(ctx, request))
                            .compose(ctx -> runSteps(execution, engine, driver, featureFile, ctx))
                            .compose(v   -> { execution.complete(); return closeDriver(driver); })
                            .recover(err -> { execution.abort(err.getMessage()); return closeDriver(driver); });
                })
                .map(v -> {
                    stateStore.saveState(execution);
                    kafka.publishExecutionCompleted(execution, device);

                    return new JsonObject()
                            .put("executionId", execution.getExecutionId())
                            .put("status", execution.getStatus().name())
                            .put("device", device.name())
                            .put("platform", device.platform().name())
                            .put("durationMillis", execution.durationMillis())
                            .put("totalSteps", execution.getStepResults().size());
                });
    }

    private Future<Void> runSteps(TestExecution execution, TestEngine engine,
                                  ManagedDriver driver, JsonObject featureFile,
                                  JsonObject context) {
        var scenarios = featureFile.getJsonArray("scenarios", new JsonArray());
        Future<Void> chain = Future.succeededFuture();

        for (int s = 0; s < scenarios.size(); s++) {
            var steps = scenarios.getJsonObject(s).getJsonArray("steps", new JsonArray());
            for (int i = 0; i < steps.size(); i++) {
                var step    = steps.getJsonObject(i);
                var text    = step.getString("text");
                var keyword = step.getString("keyword");
                var action  = step.getJsonObject("action");

                chain = chain.compose(v -> {
                    int idx = execution.getCurrentStepIndex();
                    return engine.executeStep(driver, idx, text, keyword, action, context)
                            .map(result -> {
                                execution.recordStepResult(result);
                                stateStore.saveState(execution);
                                kafka.publishStepCompleted(execution, result);
                                if (result.status() == StepStatus.FAILED) {
                                    throw new RuntimeException("Step failed: " + result.errorMessage());
                                }
                                return (Void) null;
                            });
                });
            }
        }
        return chain;
    }

    private Future<JsonObject> mergeEnvironmentVars(JsonObject context, JsonObject request) {
        var envConfig = request.getJsonObject("environmentConfig", new JsonObject());
        var vars = context.getJsonObject("variables", new JsonObject());
        envConfig.forEach(e -> vars.put(e.getKey(), e.getValue()));
        context.put("variables", vars);
        return Future.succeededFuture(context);
    }

    private Future<Void> closeDriver(ManagedDriver driver) {
        return Future.future(p -> {
            try {
                driver.close();
            } catch (Exception ignored) {
            }
            p.complete();
        });
    }
}