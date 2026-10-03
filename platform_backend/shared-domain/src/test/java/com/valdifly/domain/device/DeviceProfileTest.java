package com.valdifly.domain.device;

import com.valdifly.domain.device.mobile.MobileDeviceProfile;
import com.valdifly.domain.device.mobile.android.AndroidApp;
import com.valdifly.domain.device.valueobject.BrowserType;
import com.valdifly.domain.device.valueobject.DeviceProfileId;
import com.valdifly.domain.device.mobile.ios.IosApp;
import com.valdifly.domain.device.mobile.MobileApp;
import com.valdifly.domain.device.mobile.MobileOs;
import com.valdifly.domain.device.web.WebDeviceProfile;
import com.valdifly.domain.device.valueobject.OSType;
import com.valdifly.domain.device.valueobject.PlatformType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceProfileTest {

    @Test
    void webProfileRequiresBrowser() {
        assertThrows(IllegalArgumentException.class, () ->
                DeviceProfile.create("Chrome", PlatformType.WEB_DESKTOP, OSType.LINUX, null));
        assertThrows(IllegalArgumentException.class, () ->
                WebDeviceProfile.create("Chrome", PlatformType.WEB_DESKTOP, OSType.LINUX, null));
        assertThrows(IllegalArgumentException.class, () ->
                WebDeviceProfile.create("Chrome", PlatformType.NATIVE_MOBILE, OSType.LINUX, BrowserType.CHROME));
    }

    @Test
    void nativeProfileRequiresMobileOs() {
        assertThrows(IllegalArgumentException.class, () ->
                DeviceProfile.create("Phone", PlatformType.NATIVE_MOBILE, OSType.LINUX, null));
        assertThrows(IllegalArgumentException.class, () -> MobileOs.from(OSType.WINDOWS));
        assertThrows(IllegalArgumentException.class, () -> MobileOs.from(null));
    }

    @Test
    void createWebPresetWithoutUrls() {
        DeviceProfile profile = DeviceProfile.create(
                "Chrome desktop", PlatformType.WEB_DESKTOP, OSType.LINUX, BrowserType.CHROME
        );
        WebDeviceProfile web = assertInstanceOf(WebDeviceProfile.class, profile);
        web.describeBrowser(BrowserType.CHROME, "120");
        web.describeOs(OSType.LINUX, "120");
        web.configureHeadless(true);
        web.describeScreenResolution("1920x1080");
        profile.setExtraCapability("acceptInsecureCerts", "true");

        assertEquals(PlatformType.WEB_DESKTOP, profile.getPlatform());
        assertEquals(BrowserType.CHROME, web.getBrowser());
        assertEquals("120", web.getBrowserVersion());
        assertEquals(OSType.LINUX, web.getOs());
        assertTrue(web.isHeadless());
        assertEquals("1920x1080", web.getScreenResolution());
        assertEquals(Map.of("acceptInsecureCerts", "true"), profile.getExtraCapabilities());
        assertTrue(profile.isActive());
    }

    @Test
    void createNativePreset() {
        MobileDeviceProfile mobile = assertInstanceOf(MobileDeviceProfile.class, DeviceProfile.create(
                "Pixel 8", PlatformType.NATIVE_MOBILE, OSType.ANDROID, null
        ));
        mobile.describeDevice("Pixel_8");
        mobile.installApp(new AndroidApp("com.shop", ".Main", "/app.apk"));
        mobile.describeAutomation("UiAutomator2");

        assertEquals(MobileOs.ANDROID, mobile.getOs());
        AndroidApp app = assertInstanceOf(AndroidApp.class, mobile.getApp());
        assertEquals("com.shop", app.appPackage());
        assertEquals(".Main", app.appActivity());
        assertEquals("/app.apk", app.appPath());
        assertEquals("UiAutomator2", mobile.getAutomationName());
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
        profile.replaceExtraCapabilities(Map.of("timeout", "60"));
        profile.activate();
        assertEquals(Map.of("timeout", "60"), profile.getExtraCapabilities());
        assertTrue(profile.isActive());
        assertThrows(IllegalArgumentException.class, () -> profile.rename("  "));
        assertThrows(IllegalArgumentException.class, () -> profile.setExtraCapability("  ", "x"));
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

        WebDeviceProfile web = assertInstanceOf(WebDeviceProfile.class, restored);
        assertEquals("Safari iPhone", restored.getName());
        assertEquals("iPhone 16", restored.getDeviceName());
        assertEquals("18.2", restored.getPlatformVersion());
        assertEquals(OSType.IOS, web.getOs());
        assertFalse(restored.isActive());
        assertEquals(created, restored.getCreatedAt());
    }

    @Test
    void reconstituteDropsBrowserFieldsOnNativeProfile() {
        DeviceProfile restored = DeviceProfile.reconstitute(
                DeviceProfileId.of("dp-2"), "Phone", PlatformType.NATIVE_MOBILE,
                OSType.ANDROID, BrowserType.CHROME, "120", "Pixel_8", "14",
                "UiAutomator2", "com.shop", ".Main", "legacy-bundle", "/app.apk",
                true, "1080x2400", Map.of(), true,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z")
        );

        MobileDeviceProfile mobile = assertInstanceOf(MobileDeviceProfile.class, restored);
        assertEquals(MobileOs.ANDROID, mobile.getOs());
        assertEquals("UiAutomator2", mobile.getAutomationName());
        AndroidApp app = assertInstanceOf(AndroidApp.class, mobile.getApp());
        assertEquals("com.shop", app.appPackage());
        assertEquals(".Main", app.appActivity());
        assertEquals("/app.apk", app.appPath());
        assertTrue(mobile.isAndroidDevice());
    }

    @Test
    void reconstituteDropsAppFieldsOnWebProfile() {
        DeviceProfile restored = DeviceProfile.reconstitute(
                DeviceProfileId.of("dp-3"), "Chrome", PlatformType.WEB_DESKTOP,
                OSType.LINUX, BrowserType.CHROME, null, null, null,
                "UiAutomator2", "com.shop", ".Main", "bundle", "/app.apk",
                false, null, Map.of(), true,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z")
        );

        WebDeviceProfile web = assertInstanceOf(WebDeviceProfile.class, restored);
        assertEquals(BrowserType.CHROME, web.getBrowser());
        assertFalse(web.isAndroidDevice());
    }

    @Test
    void installAppRejectsOsMismatch() {
        MobileDeviceProfile ios = MobileDeviceProfile.create("iPhone", MobileOs.IOS);
        assertThrows(IllegalArgumentException.class, () -> ios.installApp(new AndroidApp("com.shop", null, null)));

        MobileDeviceProfile android = MobileDeviceProfile.create("Pixel", MobileOs.ANDROID);
        assertThrows(IllegalArgumentException.class, () -> android.installApp(new IosApp("com.shop.ios", null)));

        ios.installApp(new IosApp("com.shop.ios", null));
        assertFalse(ios.isAndroidDevice());
        ios.installApp(null);
        assertNull(ios.getApp());
    }

    @Test
    void osSwitchRejectsStrandedApp() {
        MobileDeviceProfile android = MobileDeviceProfile.create("Pixel", MobileOs.ANDROID);
        android.installApp(new AndroidApp("com.shop", ".Main", null));
        assertThrows(IllegalArgumentException.class, () -> android.describeOs(MobileOs.IOS, "18"));

        android.installApp(null);
        android.describeOs(MobileOs.IOS, "18");
        assertEquals(MobileOs.IOS, android.getOs());
    }

    @Test
    void appRecordsRejectHalfSpecifiedShapes() {
        assertThrows(IllegalArgumentException.class, () -> new AndroidApp(null, ".Main", null));
        assertThrows(IllegalArgumentException.class, () -> new IosApp("  ", null));
        assertEquals("/app.apk", new AndroidApp(null, null, "/app.apk").appPath());
        assertEquals("com.shop", new IosApp(" com.shop ", null).bundleId());
    }

    @Test
    void appOfFoldsFlatFieldsPerOs() {
        assertEquals(AndroidApp.class, MobileDeviceProfile.appOf(
                MobileOs.ANDROID, "com.shop", ".Main", "legacy-bundle", null).getClass());
        assertNull(MobileDeviceProfile.appOf(MobileOs.ANDROID, null, ".Main", null, null));
        assertNull(MobileDeviceProfile.appOf(MobileOs.IOS, "com.shop", ".Main", null, null));
        MobileApp ios = MobileDeviceProfile.appOf(MobileOs.IOS, null, null, "com.shop.ios", "/app.ipa");
        assertEquals(IosApp.class, ios.getClass());
    }
}
