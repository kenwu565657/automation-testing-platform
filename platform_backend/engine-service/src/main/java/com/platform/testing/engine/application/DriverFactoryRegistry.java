package com.platform.testing.engine.application;

import com.platform.testing.domain.device.DeviceProfile;
import com.platform.testing.engine.domain.driver.DriverFactory;
import com.platform.testing.engine.domain.driver.ManagedDriver;
import com.platform.testing.engine.infrastructure.driver.MobileDriverFactory;
import com.platform.testing.engine.infrastructure.driver.WebDriverFactory;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;

/**
 * Picks the right DriverFactory for a DeviceProfile.
 * Open/Closed — register more factories for BrowserStack, SauceLabs, etc.
 */
public class DriverFactoryRegistry {

    private final List<DriverFactory> factories = new ArrayList<>();

    public DriverFactoryRegistry() {
        factories.add(new WebDriverFactory());
        factories.add(new MobileDriverFactory());
    }

    public void register(DriverFactory factory) {
        factories.add(0, factory);
    }

    public Future<ManagedDriver> createDriver(DeviceProfile profile) {
        return factories.stream()
                .filter(f -> f.supports(profile.platform()))
                .findFirst()
                .map(f -> f.createDriver(profile))
                .orElse(Future.failedFuture(
                        new UnsupportedOperationException("No factory for platform: " + profile.platform())));
    }
}
