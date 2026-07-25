package com.platform.testing.engine.domain.engine;

import com.platform.testing.engine.domain.driver.ManagedDriver;
import com.platform.testing.domain.execution.StepExecutionResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * Port — unified test engine interface.
 * Each test category (UI, API, Load) implements this.
 */
public interface TestEngine {

    Future<StepExecutionResult> executeStep(
            ManagedDriver driver,
            int stepIndex,
            String stepText,
            String keyword,
            JsonObject action,
            JsonObject context
    );

    String supportedTestType();
}
