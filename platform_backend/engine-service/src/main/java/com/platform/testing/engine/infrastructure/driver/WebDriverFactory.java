package com.platform.testing.engine.infrastructure.driver;

import com.platform.testing.domain.device.Browser;
import com.platform.testing.engine.domain.device.DeviceProfile;
import com.platform.testing.domain.device.Platform;
import com.platform.testing.engine.domain.driver.DriverFactory;
import com.platform.testing.engine.domain.driver.ManagedDriver;
import io.vertx.core.Future;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;

public class WebDriverFactory implements DriverFactory {

    private static final Logger log = LoggerFactory.getLogger(WebDriverFactory.class);

    @Override
    public Future<ManagedDriver> createDriver(DeviceProfile profile) {
        return Future.future(promise -> {
            try {
                WebDriver driver;
                if (profile.gridUrl() != null) {
                    driver = createRemoteDriver(profile);
                } else {
                    driver = createLocalDriver(profile);
                }
                applyResolution(driver, profile);
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

                log.info("WebDriver created: browser={} headless={} resolution={} remote={}",
                        profile.browser(), profile.headless(), profile.screenResolution(), profile.isRemote());

                promise.complete(new ManagedDriver(driver, profile));
            } catch (Exception e) {
                log.error("Failed to create WebDriver for {}", profile.name(), e);
                promise.fail(e);
            }
        });
    }

    @Override
    public boolean supports(Platform platform) {
        return platform == Platform.WEB;
    }

    // ---- Local drivers ----

    private WebDriver createLocalDriver(DeviceProfile profile) {
        var browser = profile.browser() != null ? profile.browser() : Browser.CHROME;
        return switch (browser) {
            case CHROME  -> chromeDriver(profile);
            case FIREFOX -> firefoxDriver(profile);
            case SAFARI  -> safariDriver(profile);
            case EDGE    -> edgeDriver(profile);
        };
    }

    private ChromeDriver chromeDriver(DeviceProfile p) {
        var opts = new ChromeOptions();
        if (p.headless()) opts.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--disable-dev-shm-usage");
        if (p.screenResolution() != null) opts.addArguments("--window-size=" + p.screenResolution().replace("x", ","));
        p.extraCapabilities().forEach(opts::setCapability);
        return new ChromeDriver(opts);
    }

    private FirefoxDriver firefoxDriver(DeviceProfile p) {
        var opts = new FirefoxOptions();
        if (p.headless()) opts.addArguments("-headless");
        p.extraCapabilities().forEach(opts::setCapability);
        return new FirefoxDriver(opts);
    }

    private SafariDriver safariDriver(DeviceProfile p) {
        var opts = new SafariOptions();
        p.extraCapabilities().forEach(opts::setCapability);
        return new SafariDriver(opts);
    }

    private EdgeDriver edgeDriver(DeviceProfile p) {
        var opts = new EdgeOptions();
        if (p.headless()) opts.addArguments("--headless=new");
        p.extraCapabilities().forEach(opts::setCapability);
        return new EdgeDriver(opts);
    }

    // ---- Remote Grid driver ----

    private WebDriver createRemoteDriver(DeviceProfile profile) throws Exception {
        var browser = profile.browser() != null ? profile.browser() : Browser.CHROME;
        var gridUrl = new URI(profile.gridUrl()).toURL();

        return switch (browser) {
            case CHROME -> {
                var opts = new ChromeOptions();
                if (profile.headless()) opts.addArguments("--headless=new", "--no-sandbox", "--disable-gpu");
                applyVersion(opts, profile);
                profile.extraCapabilities().forEach(opts::setCapability);
                yield new RemoteWebDriver(gridUrl, opts);
            }
            case FIREFOX -> {
                var opts = new FirefoxOptions();
                if (profile.headless()) opts.addArguments("-headless");
                applyVersion(opts, profile);
                profile.extraCapabilities().forEach(opts::setCapability);
                yield new RemoteWebDriver(gridUrl, opts);
            }
            case SAFARI -> {
                var opts = new SafariOptions();
                profile.extraCapabilities().forEach(opts::setCapability);
                yield new RemoteWebDriver(gridUrl, opts);
            }
            case EDGE -> {
                var opts = new EdgeOptions();
                if (profile.headless()) opts.addArguments("--headless=new");
                applyVersion(opts, profile);
                profile.extraCapabilities().forEach(opts::setCapability);
                yield new RemoteWebDriver(gridUrl, opts);
            }
        };
    }

    private void applyVersion(org.openqa.selenium.MutableCapabilities opts, DeviceProfile p) {
        if (p.browserVersion() != null && !"latest".equals(p.browserVersion())) {
            opts.setCapability("browserVersion", p.browserVersion());
        }
    }

    private void applyResolution(WebDriver driver, DeviceProfile p) {
        if (p.screenResolution() != null) {
            try {
                var parts = p.screenResolution().split("x");
                driver.manage().window().setSize(new Dimension(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
            } catch (Exception e) {
                log.warn("Could not set resolution: {}", p.screenResolution());
            }
        }
    }
}