package com.platform.testing.domain.device;

import com.platform.testing.domain.common.AggregateRoot;
import com.platform.testing.domain.device.valueobject.BrowserType;
import com.platform.testing.domain.device.valueobject.DeviceProfileId;
import com.platform.testing.domain.device.valueobject.OSType;
import com.platform.testing.domain.device.valueobject.PlatformType;
import com.platform.testing.utils.TimeUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class DeviceProfile implements AggregateRoot<DeviceProfileId> {

    private final DeviceProfileId id;
    private String name;
    private final PlatformType platform;
    private OSType os;
    private BrowserType browser;
    private String browserVersion;
    private String deviceName;
    private String platformVersion;
    private String automationName;
    private String appPackage;
    private String appActivity;
    private String bundleId;
    private String appPath;
    private boolean headless;
    private String screenResolution;
    private final Map<String, String> extraCapabilities;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static DeviceProfile create(String name, PlatformType platform, OSType os, BrowserType browser) {
        return new DeviceProfile(DeviceProfileId.generate(), name, platform, os, browser);
    }

    public static DeviceProfile reconstitute(
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
        DeviceProfile profile = new DeviceProfile(id, name, platform, os, browser);
        profile.browserVersion = browserVersion;
        profile.deviceName = deviceName;
        profile.platformVersion = platformVersion;
        profile.automationName = automationName;
        profile.appPackage = appPackage;
        profile.appActivity = appActivity;
        profile.bundleId = bundleId;
        profile.appPath = appPath;
        profile.headless = headless;
        profile.screenResolution = screenResolution;
        if (extraCapabilities != null) {
            profile.extraCapabilities.putAll(extraCapabilities);
        }
        profile.active = active;
        profile.createdAt = createdAt;
        profile.updatedAt = updatedAt;
        return profile;
    }

    private DeviceProfile(DeviceProfileId id, String name, PlatformType platform, OSType os, BrowserType browser) {
        this.id = Objects.requireNonNull(id);
        this.name = requireName(name);
        this.platform = Objects.requireNonNull(platform, "platform is required");
        this.os = os;
        this.browser = browser;
        this.extraCapabilities = new LinkedHashMap<>();
        this.active = true;
        this.createdAt = TimeUtils.now();
        this.updatedAt = this.createdAt;
        validateShape();
    }

    public void rename(String newName) {
        this.name = requireName(newName);
        touch();
    }

    public void describeBrowser(BrowserType browser, String browserVersion) {
        this.browser = browser;
        this.browserVersion = browserVersion;
        validateShape();
        touch();
    }

    public void describeOs(OSType os, String platformVersion) {
        this.os = os;
        this.platformVersion = platformVersion;
        validateShape();
        touch();
    }

    public void describeDevice(String deviceName) {
        this.deviceName = deviceName;
        touch();
    }

    public void describeApp(String appPackage, String appActivity, String bundleId, String appPath) {
        this.appPackage = appPackage;
        this.appActivity = appActivity;
        this.bundleId = bundleId;
        this.appPath = appPath;
        touch();
    }

    public void describeAutomation(String automationName) {
        this.automationName = automationName;
        touch();
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
        touch();
    }

    public void setScreenResolution(String screenResolution) {
        this.screenResolution = screenResolution;
        touch();
    }

    public void setExtraCapability(String key, String value) {
        Objects.requireNonNull(key, "capability key is required");
        this.extraCapabilities.put(key, value);
        touch();
    }

    public void deactivate() {
        this.active = false;
        touch();
    }

    private void validateShape() {
        switch (platform) {
            case WEB_DESKTOP, WEB_MOBILE -> {
                if (browser == null) {
                    throw new IllegalArgumentException("browser is required for web profiles");
                }
            }
            case NATIVE_MOBILE -> {
                if (os != OSType.ANDROID && os != OSType.IOS) {
                    throw new IllegalArgumentException("native profiles require ANDROID or IOS");
                }
            }
        }
    }

    private void touch() {
        this.updatedAt = TimeUtils.now();
    }

    private static String requireName(String name) {
        Objects.requireNonNull(name, "name is required");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        return name;
    }

    @Override
    public DeviceProfileId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PlatformType getPlatform() {
        return platform;
    }

    public OSType getOs() {
        return os;
    }

    public BrowserType getBrowser() {
        return browser;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public String getAutomationName() {
        return automationName;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public String getAppActivity() {
        return appActivity;
    }

    public String getBundleId() {
        return bundleId;
    }

    public String getAppPath() {
        return appPath;
    }

    public boolean isHeadless() {
        return headless;
    }

    public String getScreenResolution() {
        return screenResolution;
    }

    public Map<String, String> getExtraCapabilities() {
        return Collections.unmodifiableMap(extraCapabilities);
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
