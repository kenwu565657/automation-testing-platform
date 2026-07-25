package com.platform.testing.engine.application;

import com.platform.testing.engine.domain.engine.ApiTestEngine;
import com.platform.testing.engine.domain.engine.LoadTestEngine;
import com.platform.testing.engine.domain.engine.TestEngine;
import com.platform.testing.engine.domain.engine.UiTestEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * Selects the correct TestEngine based on test type string.
 */
public class EngineSelector {

    private final Map<String, TestEngine> engines = new HashMap<>();

    public EngineSelector() {
        var ui   = new UiTestEngine();
        var api  = new ApiTestEngine();
        var load = new LoadTestEngine();

        engines.put("WEB_E2E",    ui);
        engines.put("MOBILE_E2E", ui);   // same engine, different driver
        engines.put("UI",         ui);
        engines.put("API",        api);
        engines.put("LOAD",       load);
    }

    public TestEngine select(String testType) {
        var engine = engines.get(testType);
        if (engine == null) {
            throw new IllegalArgumentException("No engine for test type: " + testType);
        }
        return engine;
    }

    public void register(String testType, TestEngine engine) {
        engines.put(testType, engine);
    }
}
