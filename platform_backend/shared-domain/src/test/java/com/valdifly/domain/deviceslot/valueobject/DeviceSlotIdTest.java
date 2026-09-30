package com.valdifly.domain.deviceslot.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeviceSlotIdTest {

    @Test
    void ofRejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> DeviceSlotId.of(" "));
    }

    @Test
    void generateAndOfRoundTrip() {
        DeviceSlotId id = DeviceSlotId.generate();
        assertEquals(id, DeviceSlotId.of(id.value()));
    }
}
