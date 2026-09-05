package com.platform.testing.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeUtilsTest {

    @AfterEach
    void resetClock() {
        TimeUtils.reset();
    }

    @Test
    void nowUsesFixedClock() {
        Instant fixed = Instant.parse("2026-01-01T00:00:00Z");
        TimeUtils.useClock(Clock.fixed(fixed, ZoneOffset.UTC));

        assertEquals(fixed, TimeUtils.now());
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), TimeUtils.nowDateTime());
    }
}
