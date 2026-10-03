package com.valdifly.utils;

import java.util.UUID;

/**
 * The single seam for identity generation — every ID value object's {@code generate()}
 * routes through here. Swapping the strategy (UUID → Snowflake or another time-ordered
 * scheme) is a one-line change in this class, not an edit across the domain. The method
 * name is deliberately strategy-neutral ("next", not "random").
 */
public final class IdUtils {

    private IdUtils() {}

    public static String nextId() {
        return UUID.randomUUID().toString();
    }
}
