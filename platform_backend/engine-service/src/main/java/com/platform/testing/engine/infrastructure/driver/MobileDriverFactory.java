package com.platform.testing.engine.infrastructure.driver;

import com.platform.testing.domain.device.Browser;
import com.platform.testing.engine.domain.device.DeviceProfile;
import com.platform.testing.domain.device.Platform;
import com.platform.testing.engine.domain.driver.DriverFactory;
import com.platform.testing.engine.domain.driver.ManagedDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;

public class MobileDriverFactory implements DriverFactory {

    private static final Logger log = LoggerFactory.getLogger(MobileDriverFactory.class);
    private static final String DEFAULT_APPIUM = "http://127.0.0.1:4723";

    @Override
    public Future<ManagedDriver> createDriver(DeviceProfile profile) {
        return Future.future(promise -> {
            try {
                var url = new URI(profile.appiumUrl() != null ? profile.appiumUrl() : DEFAULT_APPIUM).toURL();

                var driver = switch (profile.platform()) {
                    case ANDROID -> createAndroidDriver(url, profile);
                    case IOS     -> createIosDriver(url, profile);
                    case WEB     -> throw new IllegalArgumentException("MobileDriverFactory does not handle WEB");
                };

                log.info("Mobile driver created: platform={} device={} session={}",
                        profile.platform(), profile.deviceName(), driver.getSessionId());

                promise.complete(new ManagedDriver(driver, profile));
            } catch (Exception e) {
                log.error("Failed to create mobile driver for {}", profile.name(), e);
                promise.fail(e);
            }
        });
    }

    @Override
    public boolean supports(Platform platform) {
        return platform == Platform.ANDROID || platform == Platform.IOS;
    }

    // ---- Android ----

    private AndroidDriver createAndroidDriver(java.net.URL url, DeviceProfile p) {
        var opts = new UiAutomator2Options();

        if (p.deviceName() != null)      opts.setDeviceName(p.deviceName());
        if (p.platformVersion() != null) opts.setPlatformVersion(p.platformVersion());
        if (p.appPackage() != null)      opts.setAppPackage(p.appPackage());
        if (p.appActivity() != null)     opts.setAppActivity(p.appActivity());
        if (p.appPath() != null)         opts.setApp(p.appPath());

        // Mobile browser testing (Chrome on Android)
        if (p.browser() == Browser.CHROME && p.appPackage() == null) {
            opts.withBrowserName("Chrome");
        }

        opts.setAutomationName(p.automationName() != null ? p.automationName() : "UiAutomator2");
        opts.setNewCommandTimeout(Duration.ofSeconds(60));
        p.extraCapabilities().forEach(opts::setCapability);

        return new AndroidDriver(url, opts);
    }

    // ---- iOS ----

    private IOSDriver createIosDriver(java.net.URL url, DeviceProfile p) {
        var opts = new XCUITestOptions();

        if (p.deviceName() != null)      opts.setDeviceName(p.deviceName());
        if (p.platformVersion() != null) opts.setPlatformVersion(p.platformVersion());
        if (p.bundleId() != null)        opts.setBundleId(p.bundleId());
        if (p.appPath() != null)         opts.setApp(p.appPath());

        // Mobile browser testing (Safari on iOS)
        if (p.browser() == Browser.SAFARI && p.bundleId() == null) {
            opts.withBrowserName("Safari");
        }

        opts.setAutomationName(p.automationName() != null ? p.automationName() : "XCUITest");
        opts.setNewCommandTimeout(Duration.ofSeconds(60));
        p.extraCapabilities().forEach(opts::setCapability);

        return new IOSDriver(url, opts);
    }
}
