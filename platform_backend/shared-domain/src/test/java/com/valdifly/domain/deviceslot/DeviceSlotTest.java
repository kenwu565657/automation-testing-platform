package com.valdifly.domain.deviceslot;

import com.valdifly.domain.device.valueobject.DeviceProfileId;
import com.valdifly.domain.deviceslot.valueobject.DeviceSlotStatus;
import com.valdifly.domain.execution.valueobject.TestRunId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeviceSlotTest {

    @Test
    void createStartsAvailable() {
        DeviceSlot slot = DeviceSlot.create("pixel-5", DeviceProfileId.generate(), "http://appium:4723");
        assertEquals(DeviceSlotStatus.AVAILABLE, slot.getStatus());
        assertNull(slot.getAllocatedRunId());
    }

    @Test
    void createRejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
                DeviceSlot.create("  ", DeviceProfileId.generate(), "http://grid:4444"));
    }

    @Test
    void createRejectsBlankRemoteUrl() {
        assertThrows(IllegalArgumentException.class, () ->
                DeviceSlot.create("chrome-1", DeviceProfileId.generate(), " "));
    }

    @Test
    void allocateThenRelease() {
        DeviceSlot slot = DeviceSlot.create("chrome-1", DeviceProfileId.generate(), "http://grid:4444");
        TestRunId runId = TestRunId.of("run-1");
        slot.allocate(runId);
        assertEquals(DeviceSlotStatus.ALLOCATED, slot.getStatus());
        assertEquals(runId, slot.getAllocatedRunId());

        slot.release();
        assertEquals(DeviceSlotStatus.AVAILABLE, slot.getStatus());
        assertNull(slot.getAllocatedRunId());
        assertNull(slot.getAllocatedAt());
    }

    @Test
    void allocateRejectsWhenNotAvailable() {
        DeviceSlot slot = DeviceSlot.create("chrome-1", DeviceProfileId.generate(), "http://grid:4444");
        slot.takeOffline();
        assertThrows(IllegalStateException.class, () -> slot.allocate(TestRunId.of("run-1")));
    }

    @Test
    void cannotTakeAllocatedSlotOffline() {
        DeviceSlot slot = DeviceSlot.create("chrome-1", DeviceProfileId.generate(), "http://grid:4444");
        slot.allocate(TestRunId.of("run-1"));
        assertThrows(IllegalStateException.class, slot::takeOffline);
    }

    @Test
    void bringOnlineFromOffline() {
        DeviceSlot slot = DeviceSlot.create("chrome-1", DeviceProfileId.generate(), "http://grid:4444");
        slot.takeOffline();
        slot.bringOnline();
        assertEquals(DeviceSlotStatus.AVAILABLE, slot.getStatus());
    }

    @Test
    void releaseIsIdempotentWhenAvailable() {
        DeviceSlot slot = DeviceSlot.create("chrome-1", DeviceProfileId.generate(), "http://grid:4444");
        slot.release();
        assertEquals(DeviceSlotStatus.AVAILABLE, slot.getStatus());
    }

    @Test
    void cannotReleaseOfflineSlot() {
        DeviceSlot slot = DeviceSlot.create("chrome-1", DeviceProfileId.generate(), "http://grid:4444");
        slot.takeOffline();
        assertThrows(IllegalStateException.class, slot::release);
    }
}
