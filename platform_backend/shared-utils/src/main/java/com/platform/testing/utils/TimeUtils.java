package com.platform.testing.utils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * Current time. Call {@link #now()} everywhere instead of {@code Instant.now()}.
 */
public final class TimeUtils {

    private static volatile Clock clock = Clock.systemUTC();

    private TimeUtils() {}

    public static Instant now() {
        return Instant.now(clock);
    }

    public static LocalDateTime nowDateTime() {
        return LocalDateTime.ofInstant(now(), ZoneOffset.UTC);
    }

    /** Test hook. Pair with {@link #reset()}. */
    public static void useClock(Clock newClock) {
        clock = Objects.requireNonNull(newClock);
    }

    public static void reset() {
        clock = Clock.systemUTC();
    }
}
