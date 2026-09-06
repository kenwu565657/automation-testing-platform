package com.platform.testing.domain.pageobject.valueobject;

import com.platform.testing.domain.device.valueobject.PlatformType;

import java.util.Objects;

public record ElementLocator(
        PlatformType platformType,
        LocatorStrategy locatorStrategy,
        String locatorValue,
        LocatorStrategy fallbackStrategy,
        String fallbackValue
) {
    public ElementLocator {
        Objects.requireNonNull(platformType, "platformType is required");
        Objects.requireNonNull(locatorStrategy, "locatorStrategy is required");
        Objects.requireNonNull(locatorValue, "locatorValue is required");
    }
}
