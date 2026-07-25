package com.platform.testing.engine.domain.device;

import com.platform.testing.domain.device.Browser;
import com.platform.testing.domain.device.Platform;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class DeviceProfileTest {

    @Test
    void chromeDesktopFactory() {
        var profile = DeviceProfile.chromeDesktop();
        assertThat(profile.platform()).isEqualTo(Platform.WEB);
        assertThat(profile.browser()).isEqualTo(Browser.CHROME);
        assertThat(profile.isMobile()).isFalse();
        assertThat(profile.isRemote()).isFalse();
    }

    @Test
    void androidDeviceIsMobile() {
        var profile = DeviceProfile.androidDevice(
                "http://localhost:4723", "Pixel 7", "com.example", ".MainActivity");
        assertThat(profile.isMobile()).isTrue();
        assertThat(profile.platform()).isEqualTo(Platform.ANDROID);
        assertThat(profile.automationName()).isEqualTo("UiAutomator2");
    }

    @Test
    void iosDeviceIsMobile() {
        var profile = DeviceProfile.iosDevice(
                "http://localhost:4723", "iPhone 15 Pro", "com.example.app");
        assertThat(profile.isMobile()).isTrue();
        assertThat(profile.platform()).isEqualTo(Platform.IOS);
        assertThat(profile.automationName()).isEqualTo("XCUITest");
    }

    @Test
    void remoteDetection() {
        var local = DeviceProfile.chromeDesktop();
        assertThat(local.isRemote()).isFalse();

        var grid = DeviceProfile.chromeGrid("http://selenium-grid:4444");
        assertThat(grid.isRemote()).isTrue();
    }

    @Test
    void fromJsonWithDefaults() {
        var profile = DeviceProfile.fromJson(null);
        assertThat(profile.platform()).isEqualTo(Platform.WEB);
        assertThat(profile.browser()).isEqualTo(Browser.CHROME);
    }

    @Test
    void fromJsonFull() {
        var json = new JsonObject()
                .put("id", "my-device")
                .put("name", "Test Device")
                .put("platform", "ANDROID")
                .put("deviceName", "Pixel 8")
                .put("platformVersion", "15")
                .put("automationName", "UiAutomator2")
                .put("appPackage", "com.example")
                .put("appActivity", ".Main")
                .put("appiumUrl", "http://appium:4723")
                .put("headless", false)
                .put("screenResolution", "1080x2400")
                .put("extraCapabilities", new JsonObject());

        var profile = DeviceProfile.fromJson(json);
        assertThat(profile.platform()).isEqualTo(Platform.ANDROID);
        assertThat(profile.deviceName()).isEqualTo("Pixel 8");
        assertThat(profile.isMobile()).isTrue();
    }

    @Test
    void toJsonRoundTrip() {
        var original = DeviceProfile.chromeDesktop();
        var json     = original.toJson();
        var restored = DeviceProfile.fromJson(json);

        assertThat(restored.id()).isEqualTo(original.id());
        assertThat(restored.platform()).isEqualTo(original.platform());
        assertThat(restored.browser()).isEqualTo(original.browser());
        assertThat(restored.headless()).isEqualTo(original.headless());
    }
}
