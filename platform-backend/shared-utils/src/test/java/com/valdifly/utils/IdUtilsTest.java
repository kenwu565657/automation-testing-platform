package com.valdifly.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class IdUtilsTest {

    @Test
    void generatesDistinctNonBlankIds() {
        String first = IdUtils.nextId();
        String second = IdUtils.nextId();

        // The contract is only distinctness + non-blankness — the format itself is the
        // strategy's private affair (see TimeUtils' swappable Clock). If the strategy
        // swaps to Snowflake, only the length assert below needs revisiting.
        assertNotEquals(first, second);
        assertFalse(first.isBlank());
        assertEquals(36, first.length());
    }

    @Test
    void noCollisionsAcrossABatch() {
        // The property the domain actually relies on: PKs/ids generated in a burst
        // (one run dispatching a suite of cases) never collide. This is exactly what
        // a future strategy swap (UUID → Snowflake with a bad worker-id config) would break.
        Set<String> seen = new HashSet<>();
        int count = 10_000;
        for (int i = 0; i < count; i++) {
            seen.add(IdUtils.nextId());
        }
        assertEquals(count, seen.size());
    }
}
