package com.valdifly.domain.device;

import com.valdifly.domain.common.AggregateRoot;
import com.valdifly.domain.device.mobile.MobileDeviceProfile;
import com.valdifly.domain.device.web.WebDeviceProfile;
import com.valdifly.domain.device.valueobject.BrowserType;
import com.valdifly.domain.device.valueobject.DeviceProfileId;
import com.valdifly.domain.device.mobile.MobileOs;
import com.valdifly.domain.device.valueobject.OSType;
import com.valdifly.domain.device.valueobject.PlatformType;

import java.time.Instant;
import java.util.Map;

/**
 * A reusable runner preset. The variants are the model: a WebDeviceProfile (browser session,
 * WEB_DESKTOP/WEB_MOBILE) or a MobileDeviceProfile (native app session, NATIVE_MOBILE) —
 * platform-inapplicable fields are unrepresentable, so there is no cross-field shape
 * validation to run. Variants live in the web/ and mobile/ sub-packages (unsealed: Java
 * sealed types cannot span packages on the classpath); switch sites add a default-throw arm
 * where exhaustiveness is no longer compiler-checked.
 */
public interface DeviceProfile extends AggregateRoot<DeviceProfileId> {

    String getName();

    PlatformType getPlatform();

    String getDeviceName();

    String getPlatformVersion();

    Map<String, String> getExtraCapabilities();

    boolean isActive();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    /** True when the session should use Android-style scrolling (UiScrollable) rather than the swipe loop. */
    boolean isAndroidDevice();

    void rename(String newName);

    void describeDevice(String deviceName);

    void setExtraCapability(String key, String value);

    void replaceExtraCapabilities(Map<String, String> capabilities);

    void activate();

    void deactivate();

    /**
     * Creates the variant for the platform. NATIVE_MOBILE drops {@code browser};
     * web platforms require it.
     */
    static DeviceProfile create(String name, PlatformType platform, OSType os, BrowserType browser) {
        return switch (platform) {
            case WEB_DESKTOP, WEB_MOBILE -> WebDeviceProfile.create(name, platform, os, browser);
            case NATIVE_MOBILE -> MobileDeviceProfile.create(name, MobileOs.from(os));
        };
    }

    /**
     * Reconstitutes the variant for the platform from the flat row/command shape.
     * Fields that do not apply to the variant are dropped, not rejected; fields the variant
     * requires keep their validation (web requires a browser, native requires ANDROID/IOS).
     */
    static DeviceProfile reconstitute(
            DeviceProfileId id,
            String name,
            PlatformType platform,
            OSType os,
            BrowserType browser,
            String browserVersion,
            String deviceName,
            String platformVersion,
            String automationName,
            String appPackage,
            String appActivity,
            String bundleId,
            String appPath,
            boolean headless,
            String screenResolution,
            Map<String, String> extraCapabilities,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        return switch (platform) {
            case WEB_DESKTOP, WEB_MOBILE -> WebDeviceProfile.reconstitute(
                    id, name, platform, os, browser, browserVersion, deviceName, platformVersion,
                    headless, screenResolution, extraCapabilities, active, createdAt, updatedAt);
            case NATIVE_MOBILE -> MobileDeviceProfile.reconstitute(
                    id, name, MobileOs.from(os), deviceName, platformVersion, automationName,
                    MobileDeviceProfile.appOf(MobileOs.from(os), appPackage, appActivity, bundleId, appPath),
                    extraCapabilities, active, createdAt, updatedAt);
        };
    }
}
