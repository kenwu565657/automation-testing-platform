package com.valdifly.domain.deviceslot.valueobject;

import com.valdifly.domain.common.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record DeviceSlotId(String value) implements ValueObject<String> {
    public DeviceSlotId {
        Objects.requireNonNull(value, "DeviceSlotId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("DeviceSlotId cannot be blank");
        }
    }

    public static DeviceSlotId generate() {
        return new DeviceSlotId(UUID.randomUUID().toString());
    }

    public static DeviceSlotId of(String value) {
        return new DeviceSlotId(value);
    }
}
