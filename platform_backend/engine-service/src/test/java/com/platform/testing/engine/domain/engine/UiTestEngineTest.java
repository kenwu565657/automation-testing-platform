package com.platform.testing.engine.domain.engine;

import com.testplatform.engine.domain.device.DeviceProfile;
import com.testplatform.engine.domain.device.Platform;
import com.testplatform.engine.domain.driver.ManagedDriver;
import com.testplatform.engine.domain.execution.StepStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
class UiTestEngineTest {

    private UiTestEngine engine;
    private WebDriver mockWebDriver;
    private ManagedDriver managedDriver;
    private JsonObject context;

    @BeforeEach
    void setUp() {
        engine = new UiTestEngine();
        mockWebDriver = mock(WebDriver.class);
        var profile = DeviceProfile.chromeDesktop();
        managedDriver = new ManagedDriver(mockWebDriver, profile);
        context = new JsonObject()
                .put("resolvedPageObjects", new JsonObject())
                .put("variables", new JsonObject().put("baseUrl", "https://example.com"));
    }

    @Test
    void navigateAction(Vertx vertx, VertxTestContext tc) {
        var action = new JsonObject()
                .put("actionType", "NAVIGATE")
                .put("parameters", new JsonObject().put("url", "${baseUrl}/login"));

        engine.executeStep(managedDriver, 0, "navigate to login", "GIVEN", action, context)
                .onComplete(tc.succeeding(result -> {
                    assertThat(result.status()).isEqualTo(StepStatus.PASSED);
                    verify(mockWebDriver).get("https://example.com/login");
                    tc.completeNow();
                }));
    }

    @Test
    void clickAction(Vertx vertx, VertxTestContext tc) {
        var mockElement = mock(WebElement.class);
        when(mockWebDriver.findElement(any(By.class))).thenReturn(mockElement);

        var action = new JsonObject()
                .put("actionType", "CLICK")
                .put("target", new JsonObject()
                        .put("locatorStrategy", "ID")
                        .put("locatorValue", "submit-btn"))
                .put("parameters", new JsonObject());

        engine.executeStep(managedDriver, 0, "click submit", "WHEN", action, context)
                .onComplete(tc.succeeding(result -> {
                    assertThat(result.status()).isEqualTo(StepStatus.PASSED);
                    verify(mockElement).click();
                    tc.completeNow();
                }));
    }

    @Test
    void typeAction(Vertx vertx, VertxTestContext tc) {
        var mockElement = mock(WebElement.class);
        when(mockWebDriver.findElement(any(By.class))).thenReturn(mockElement);

        var action = new JsonObject()
                .put("actionType", "TYPE")
                .put("target", new JsonObject()
                        .put("locatorStrategy", "CSS")
                        .put("locatorValue", "#username"))
                .put("parameters", new JsonObject()
                        .put("text", "admin")
                        .put("clearFirst", true));

        engine.executeStep(managedDriver, 0, "type username", "WHEN", action, context)
                .onComplete(tc.succeeding(result -> {
                    assertThat(result.status()).isEqualTo(StepStatus.PASSED);
                    verify(mockElement).clear();
                    verify(mockElement).sendKeys("admin");
                    tc.completeNow();
                }));
    }

    @Test
    void unknownActionFails(Vertx vertx, VertxTestContext tc) {
        var action = new JsonObject()
                .put("actionType", "UNKNOWN_ACTION")
                .put("parameters", new JsonObject());

        engine.executeStep(managedDriver, 0, "bad step", "WHEN", action, context)
                .onComplete(tc.succeeding(result -> {
                    assertThat(result.status()).isEqualTo(StepStatus.FAILED);
                    assertThat(result.errorMessage()).contains("Unknown action");
                    tc.completeNow();
                }));
    }

    @Test
    void mobileOnlyActionFailsOnWeb(Vertx vertx, VertxTestContext tc) {
        var action = new JsonObject()
                .put("actionType", "SWIPE")
                .put("parameters", new JsonObject().put("direction", "UP"));

        engine.executeStep(managedDriver, 0, "swipe up", "WHEN", action, context)
                .onComplete(tc.succeeding(result -> {
                    assertThat(result.status()).isEqualTo(StepStatus.FAILED);
                    assertThat(result.errorMessage()).contains("mobile-only");
                    tc.completeNow();
                }));
    }

    @Test
    void pageObjectRefResolution(Vertx vertx, VertxTestContext tc) {
        var mockElement = mock(WebElement.class);
        when(mockWebDriver.findElement(By.cssSelector("#user-input"))).thenReturn(mockElement);
        when(mockElement.getText()).thenReturn("hello");

        var ctxWithPO = context.copy().put("resolvedPageObjects", new JsonObject()
                .put("pg-login", new JsonObject()
                        .put("usernameInput", new JsonObject()
                                .put("strategy", "CSS")
                                .put("value", "#user-input"))));

        var action = new JsonObject()
                .put("actionType", "ASSERT_TEXT")
                .put("target", new JsonObject()
                        .put("locatorStrategy", "PAGE_OBJECT_REF")
                        .put("pageObjectRef", new JsonObject()
                                .put("pageObjectId", "pg-login")
                                .put("elementName", "usernameInput")))
                .put("parameters", new JsonObject()
                        .put("expected", "hello")
                        .put("comparison", "EQUALS"));

        engine.executeStep(managedDriver, 0, "assert text", "THEN", action, ctxWithPO)
                .onComplete(tc.succeeding(result -> {
                    assertThat(result.status()).isEqualTo(StepStatus.PASSED);
                    tc.completeNow();
                }));
    }
}
