package com.valdifly.utils;

import java.time.Clock;
import java.time.Instant;
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

    /** Test hook. Pair with {@link #reset()}. */
    public static void useClock(Clock newClock) {
        clock = Objects.requireNonNull(newClock);
    }

    public static void reset() {
        clock = Clock.systemUTC();
    }
}
