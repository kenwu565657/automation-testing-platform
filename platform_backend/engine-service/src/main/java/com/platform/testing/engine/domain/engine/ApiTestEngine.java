package com.platform.testing.engine.domain.engine;

import com.platform.testing.engine.domain.driver.ManagedDriver;
import com.platform.testing.domain.execution.StepExecutionResult;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API test engine — uses REST Assured.
 * ManagedDriver is not used (null); context holds last response for chaining.
 */
public class ApiTestEngine implements TestEngine {

    private static final Logger log = LoggerFactory.getLogger(ApiTestEngine.class);

    /** Shared across steps within one scenario so EXTRACT_VARIABLE works. */
    private Response lastResponse;

    @Override
    public Future<StepExecutionResult> executeStep(
            ManagedDriver driver, int stepIndex, String stepText,
            String keyword, JsonObject action, JsonObject context
    ) {
        return Future.future(promise -> {
            var start = System.currentTimeMillis();
            try {
                var actionType = action.getString("actionType");
                var params     = action.getJsonObject("parameters", new JsonObject());

                switch (actionType) {
                    case "HTTP_REQUEST" -> {
                        var method  = params.getString("method", "GET");
                        var url     = resolveVars(params.getString("url"), context);
                        var headers = params.getJsonObject("headers", new JsonObject());
                        var body    = params.getString("body");

                        var spec = RestAssured.given();
                        headers.forEach(e -> spec.header(e.getKey(), e.getValue().toString()));
                        if (body != null) spec.body(resolveVars(body, context));

                        lastResponse = switch (method.toUpperCase()) {
                            case "POST"   -> spec.post(url);
                            case "PUT"    -> spec.put(url);
                            case "DELETE" -> spec.delete(url);
                            case "PATCH"  -> spec.patch(url);
                            default       -> spec.get(url);
                        };
                    }
                    case "ASSERT_STATUS" -> {
                        var expected = params.getInteger("statusCode");
                        if (lastResponse.getStatusCode() != expected) {
                            throw new AssertionError("Expected status %d but got %d"
                                    .formatted(expected, lastResponse.getStatusCode()));
                        }
                    }
                    case "ASSERT_BODY" -> {
                        var path     = params.getString("jsonPath");
                        var expected = resolveVars(params.getString("expected"), context);
                        var actual   = lastResponse.jsonPath().getString(path);
                        if (!expected.equals(actual)) {
                            throw new AssertionError("Body[%s]: expected '%s' but got '%s'"
                                    .formatted(path, expected, actual));
                        }
                    }
                    case "ASSERT_HEADER" -> {
                        var hdr      = params.getString("headerName");
                        var expected = resolveVars(params.getString("expected"), context);
                        var actual   = lastResponse.getHeader(hdr);
                        if (actual == null || !actual.contains(expected)) {
                            throw new AssertionError("Header[%s]: expected '%s' but got '%s'"
                                    .formatted(hdr, expected, actual));
                        }
                    }
                    case "EXTRACT_VARIABLE" -> {
                        var path = params.getString("jsonPath");
                        var name = params.getString("variableName");
                        var val  = lastResponse.jsonPath().getString(path);
                        context.getJsonObject("variables", new JsonObject()).put(name, val);
                    }
                    default -> throw new UnsupportedOperationException("API action unknown: " + actionType);
                }

                var dur = System.currentTimeMillis() - start;
                promise.complete(StepExecutionResult.passed(stepIndex, stepText, keyword, dur));
            } catch (Exception e) {
                var dur = System.currentTimeMillis() - start;
                promise.complete(StepExecutionResult.failed(
                        stepIndex, stepText, keyword, dur, e.getMessage(), null));
            }
        });
    }

    @Override
    public String supportedTestType() {
        return "API";
    }

    private String resolveVars(String text, JsonObject ctx) {
        if (text == null) return null;
        var vars = ctx.getJsonObject("variables", new JsonObject());
        var r = text;
        for (var k : vars.fieldNames()) {
            r = r.replace("${" + k + "}", vars.getString(k, ""));
        }
        return r;
    }
}
