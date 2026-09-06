package com.platform.testing.domain.device.valueobject;

import com.platform.testing.domain.common.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record DeviceProfileId(String value) implements ValueObject<String> {
    public DeviceProfileId {
        Objects.requireNonNull(value, "DeviceProfileId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("DeviceProfileId cannot be blank");
        }
    }

    public static DeviceProfileId generate() {
        return new DeviceProfileId(UUID.randomUUID().toString());
    }

    public static DeviceProfileId of(String value) {
        return new DeviceProfileId(value);
    }
}
