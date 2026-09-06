package com.platform.testing.domain.schedule.valueobject;

import com.platform.testing.domain.common.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record ScheduleId(String value) implements ValueObject<String> {
    public ScheduleId {
        Objects.requireNonNull(value, "ScheduleId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ScheduleId cannot be blank");
        }
    }

    public static ScheduleId generate() {
        return new ScheduleId(UUID.randomUUID().toString());
    }

    public static ScheduleId of(String value) {
        return new ScheduleId(value);
    }
}
