package com.platform.testing.domain.device;

import com.platform.testing.domain.device.valueobject.BrowserType;
import com.platform.testing.domain.device.valueobject.DeviceProfileId;
import com.platform.testing.domain.device.valueobject.OSType;
import com.platform.testing.domain.device.valueobject.PlatformType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceProfileTest {

    @Test
    void webProfileRequiresBrowser() {
        assertThrows(IllegalArgumentException.class, () ->
                DeviceProfile.create("Chrome", PlatformType.WEB_DESKTOP, OSType.LINUX, null));
    }

    @Test
    void nativeProfileRequiresMobileOs() {
        assertThrows(IllegalArgumentException.class, () ->
                DeviceProfile.create("Phone", PlatformType.NATIVE_MOBILE, OSType.LINUX, null));
    }

    @Test
    void createWebPresetWithoutUrls() {
        DeviceProfile profile = DeviceProfile.create(
                "Chrome desktop", PlatformType.WEB_DESKTOP, OSType.LINUX, BrowserType.CHROME
        );
        profile.describeBrowser(BrowserType.CHROME, "120");
        profile.setHeadless(true);
        profile.setScreenResolution("1920x1080");
        profile.setExtraCapability("acceptInsecureCerts", "true");

        assertEquals(PlatformType.WEB_DESKTOP, profile.getPlatform());
        assertEquals(BrowserType.CHROME, profile.getBrowser());
        assertTrue(profile.isHeadless());
        assertEquals(Map.of("acceptInsecureCerts", "true"), profile.getExtraCapabilities());
        assertTrue(profile.isActive());
    }

    @Test
    void createNativePreset() {
        DeviceProfile profile = DeviceProfile.create(
                "Pixel 8", PlatformType.NATIVE_MOBILE, OSType.ANDROID, null
        );
        profile.describeDevice("Pixel_8");
        profile.describeApp("com.shop", ".Main", null, "/app.apk");
        profile.describeAutomation("UiAutomator2");

        assertEquals(OSType.ANDROID, profile.getOs());
        assertEquals("com.shop", profile.getAppPackage());
        assertEquals("UiAutomator2", profile.getAutomationName());
    }

    @Test
    void renameAndDeactivate() {
        DeviceProfile profile = DeviceProfile.create(
                "Chrome", PlatformType.WEB_DESKTOP, OSType.MACOS, BrowserType.CHROME
        );
        profile.rename("Chrome latest");
        profile.deactivate();

        assertEquals("Chrome latest", profile.getName());
        assertFalse(profile.isActive());
        assertThrows(IllegalArgumentException.class, () -> profile.rename("  "));
    }

    @Test
    void reconstituteRestoresPreset() {
        Instant created = Instant.parse("2026-01-01T00:00:00Z");
        DeviceProfile restored = DeviceProfile.reconstitute(
                DeviceProfileId.of("dp-1"), "Safari iPhone", PlatformType.WEB_MOBILE,
                OSType.IOS, BrowserType.SAFARI, "18", "iPhone 16", "18.2",
                "XCUITest", null, null, null, null,
                false, "390x844", Map.of("udid", "abc"),
                false, created, created
        );

        assertEquals("Safari iPhone", restored.getName());
        assertEquals("iPhone 16", restored.getDeviceName());
        assertFalse(restored.isActive());
        assertEquals(created, restored.getCreatedAt());
    }
}
