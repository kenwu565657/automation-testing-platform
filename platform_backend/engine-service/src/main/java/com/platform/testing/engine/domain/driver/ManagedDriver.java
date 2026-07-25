package com.platform.testing.engine.domain.driver;

import com.platform.testing.engine.domain.device.DeviceProfile;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Unified wrapper around Selenium WebDriver and Appium drivers.
 *
 * Test steps interact with ManagedDriver only — never with the
 * underlying driver directly. This makes the same step definition
 * work across Chrome, Firefox, Safari, Android, and iOS.
 */
public class ManagedDriver implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ManagedDriver.class);

    private final WebDriver driver;
    private final DeviceProfile profile;
    private final String sessionId;

    public ManagedDriver(WebDriver driver, DeviceProfile profile) {
        this.driver = driver;
        this.profile = profile;
        this.sessionId = (driver instanceof RemoteWebDriver rwd)
                ? rwd.getSessionId().toString()
                : "local";
    }

    // ---- Navigation ----

    public void navigate(String url) {
        driver.get(url);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getTitle() {
        return driver.getTitle();
    }

    // ---- Element Interaction ----

    public WebElement findElement(By locator) {
        return driver.findElement(locator);
    }

    public List<WebElement> findElements(By locator) {
        return driver.findElements(locator);
    }

    public void click(By locator) {
        findElement(locator).click();
    }

    public void type(By locator, String text, boolean clearFirst) {
        var element = findElement(locator);
        if (clearFirst) {
            element.clear();
        }
        element.sendKeys(text);
    }

    public String getText(By locator) {
        return findElement(locator).getText();
    }

    public String getAttribute(By locator, String attribute) {
        return findElement(locator).getAttribute(attribute);
    }

    // ---- Waits ----

    public WebElement waitForVisible(By locator, Duration timeout) {
        return new WebDriverWait(driver, timeout)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForClickable(By locator, Duration timeout) {
        return new WebDriverWait(driver, timeout)
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    public boolean waitForInvisible(By locator, Duration timeout) {
        return new WebDriverWait(driver, timeout)
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public void waitForUrl(String urlPart, Duration timeout) {
        new WebDriverWait(driver, timeout)
                .until(ExpectedConditions.urlContains(urlPart));
    }

    // ---- Assertions ----

    public boolean isElementVisible(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isElementPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    // ---- Screenshots ----

    public String captureScreenshotBase64() {
        if (driver instanceof TakesScreenshot ts) {
            try {
                return ts.getScreenshotAs(OutputType.BASE64);
            } catch (Exception e) {
                log.warn("Failed to capture screenshot", e);
            }
        }
        return null;
    }

    // ---- Device info ----

    public boolean isMobile() {
        return profile.isMobile();
    }

    public DeviceProfile getProfile() {
        return profile;
    }

    public String getSessionId() {
        return sessionId;
    }

    public WebDriver unwrap() {
        return driver;
    }

    @Override
    public void close() {
        try {
            driver.quit();
            log.info("Driver closed: session={}, device={}", sessionId, profile.name());
        } catch (Exception e) {
            log.warn("Error closing driver for session {}", sessionId, e);
        }
    }
}
