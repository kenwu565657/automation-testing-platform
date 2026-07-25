package com.platform.testing.engine.domain.engine;

import com.platform.testing.engine.domain.driver.ManagedDriver;
import com.platform.testing.domain.execution.StepExecutionResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Unified UI engine — same code runs on web browsers AND mobile devices.
 * The ManagedDriver wraps the correct Selenium/Appium driver.
 */
public class UiTestEngine implements TestEngine {

    private static final Logger log = LoggerFactory.getLogger(UiTestEngine.class);

    @Override
    public Future<StepExecutionResult> executeStep(
            ManagedDriver driver, int stepIndex, String stepText,
            String keyword, JsonObject action, JsonObject context
    ) {
        return Future.future(promise -> {
            var start = System.currentTimeMillis();
            try {
                var actionType = action.getString("actionType");
                var target = action.getJsonObject("target");
                var params = action.getJsonObject("parameters", new JsonObject());

                dispatch(driver, actionType, target, params, context);

                var dur = System.currentTimeMillis() - start;
                promise.complete(StepExecutionResult.passed(stepIndex, stepText, keyword, dur));
            } catch (Exception e) {
                var dur = System.currentTimeMillis() - start;
                var shot = driver.captureScreenshotBase64();
                log.warn("Step {} failed: {}", stepIndex, e.getMessage());
                promise.complete(StepExecutionResult.failed(
                        stepIndex, stepText, keyword, dur, e.getMessage(), shot));
            }
        });
    }

    @Override
    public String supportedTestType() {
        return "UI";
    }

    // ---------------------------------------------------------------
    // Action dispatcher
    // ---------------------------------------------------------------

    private void dispatch(ManagedDriver driver, String actionType,
                          JsonObject target, JsonObject params,
                          JsonObject context) throws Exception {
        switch (actionType) {
            // ---- Common ----
            case "NAVIGATE"           -> driver.navigate(resolveVars(params.getString("url"), context));
            case "CLICK"              -> driver.click(locator(target, context));
            case "TYPE"               -> driver.type(locator(target, context),
                    resolveVars(params.getString("text"), context),
                    params.getBoolean("clearFirst", false));
            case "ASSERT_VISIBLE"     -> driver.waitForVisible(locator(target, context),
                    Duration.ofSeconds(params.getInteger("timeout", 10)));
            case "ASSERT_TEXT"        -> assertText(driver, target, params, context);
            case "WAIT_FOR_CLICKABLE" -> driver.waitForClickable(locator(target, context),
                    Duration.ofSeconds(params.getInteger("timeout", 10)));
            case "WAIT_FOR_URL"       -> driver.waitForUrl(
                    resolveVars(params.getString("urlPart"), context),
                    Duration.ofSeconds(params.getInteger("timeout", 10)));
            case "WAIT"               -> Thread.sleep(params.getLong("millis", 1000L));
            case "SCREENSHOT"         -> driver.captureScreenshotBase64();
            case "SCROLL_TO_ELEMENT"  -> scrollTo(driver, target, context);

            // ---- Mobile only ----
            case "TAP"                -> { ensureMobile(driver, "TAP");  driver.click(locator(target, context)); }
            case "SWIPE"              -> { ensureMobile(driver, "SWIPE"); swipe(driver, params); }
            case "LONG_PRESS"         -> { ensureMobile(driver, "LONG_PRESS"); longPress(driver, target, context); }

            default -> throw new UnsupportedOperationException("Unknown action: " + actionType);
        }
    }

    // ---------------------------------------------------------------
    // Locator resolution  (supports PageObject refs)
    // ---------------------------------------------------------------

    private By locator(JsonObject target, JsonObject context) {
        if (target == null) throw new IllegalArgumentException("Step target is required");

        var strategy = target.getString("locatorStrategy");
        String value;

        if ("PAGE_OBJECT_REF".equals(strategy)) {
            var ref      = target.getJsonObject("pageObjectRef");
            var poId     = ref.getString("pageObjectId");
            var elName   = ref.getString("elementName");
            var resolved = context.getJsonObject("resolvedPageObjects", new JsonObject());
            var loc      = resolved.getJsonObject(poId, new JsonObject())
                    .getJsonObject(elName, new JsonObject());
            strategy = loc.getString("strategy", "CSS");
            value    = loc.getString("value");
            if (value == null) {
                throw new IllegalArgumentException("PageObject element not found: %s.%s".formatted(poId, elName));
            }
        } else {
            value = target.getString("locatorValue");
        }

        return toBy(strategy, value);
    }

    private static By toBy(String strategy, String value) {
        return switch (strategy) {
            case "XPATH"            -> By.xpath(value);
            case "ID"               -> By.id(value);
            case "NAME"             -> By.name(value);
            case "CLASS_NAME"       -> By.className(value);
            case "TAG_NAME"         -> By.tagName(value);
            case "LINK_TEXT"        -> By.linkText(value);
            case "ACCESSIBILITY_ID" -> By.xpath("//*[@content-desc='" + value + "']");
            default                 -> By.cssSelector(value); // CSS is default
        };
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private void assertText(ManagedDriver driver, JsonObject target,
                            JsonObject params, JsonObject context) {
        var actual     = driver.getText(locator(target, context));
        var expected   = resolveVars(params.getString("expected"), context);
        var comparison = params.getString("comparison", "EQUALS");
        var ok = switch (comparison) {
            case "CONTAINS"    -> actual.contains(expected);
            case "STARTS_WITH" -> actual.startsWith(expected);
            case "ENDS_WITH"   -> actual.endsWith(expected);
            case "REGEX"       -> actual.matches(expected);
            case "NOT_EQUALS"  -> !actual.equals(expected);
            default            -> actual.equals(expected);
        };
        if (!ok) {
            throw new AssertionError("Expected '%s' (%s) but got '%s'".formatted(expected, comparison, actual));
        }
    }

    private void ensureMobile(ManagedDriver d, String action) {
        if (!d.isMobile()) {
            throw new UnsupportedOperationException(
                    "'%s' is mobile-only, current platform: %s".formatted(action, d.getProfile().platform()));
        }
    }

    private void swipe(ManagedDriver driver, JsonObject params) {
        log.info("Swipe {}", params.getString("direction", "UP"));
        // TODO: implement via Appium W3C PointerInput actions
    }

    private void longPress(ManagedDriver driver, JsonObject target, JsonObject ctx) {
        log.info("Long press on {}", locator(target, ctx));
        // TODO: implement via Appium W3C PointerInput actions
    }

    private void scrollTo(ManagedDriver driver, JsonObject target, JsonObject ctx) {
        log.info("Scroll to {}", locator(target, ctx));
        // TODO: JS scroll for web, UiScrollable for Android
    }

    private String resolveVars(String text, JsonObject context) {
        if (text == null) return null;
        var vars = context.getJsonObject("variables", new JsonObject());
        var result = text;
        for (var key : vars.fieldNames()) {
            result = result.replace("${" + key + "}", vars.getString(key, ""));
        }
        return result;
    }
}
