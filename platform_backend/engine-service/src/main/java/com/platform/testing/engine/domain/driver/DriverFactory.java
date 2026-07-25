package com.platform.testing.engine.domain.driver;

import com.platform.testing.engine.domain.device.DeviceProfile;
import com.platform.testing.domain.device.Platform;
import io.vertx.core.Future;

/**
 * Port — creates the right WebDriver/AppiumDriver for a DeviceProfile.
 */
public interface DriverFactory {

    Future<ManagedDriver> createDriver(DeviceProfile profile);

    boolean supports(Platform platform);
}
