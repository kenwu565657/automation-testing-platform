package com.platform.testing.domain.target;

import com.platform.testing.domain.device.valueobject.DeviceProfileId;
import com.platform.testing.domain.project.valueobject.ProjectId;
import com.platform.testing.domain.target.valueobject.ExecutionTargetId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionTargetTest {

    @Test
    void createBindsProjectAndProfile() {
        ProjectId projectId = ProjectId.generate();
        DeviceProfileId profileId = DeviceProfileId.generate();
        ExecutionTarget target = ExecutionTarget.create(projectId, profileId, "Grid Chrome", "http://grid:4444");

        assertEquals(projectId, target.getProjectId());
        assertEquals(profileId, target.getDeviceProfileId());
        assertEquals("Grid Chrome", target.getName());
        assertEquals("http://grid:4444", target.getRemoteUrl());
        assertTrue(target.getExtraCapabilities().isEmpty());
        assertTrue(target.isActive());
    }

    @Test
    void localTargetAllowsNullRemoteUrl() {
        ExecutionTarget target = ExecutionTarget.create(
                ProjectId.generate(), DeviceProfileId.generate(), "Local Chrome", null
        );
        assertNull(target.getRemoteUrl());
    }

    @Test
    void updateDestinationWithoutChangingProfile() {
        ExecutionTarget target = ExecutionTarget.create(
                ProjectId.generate(), DeviceProfileId.generate(), "Grid", "http://grid:4444"
        );
        target.rename("Prod grid");
        target.updateRemoteUrl("http://grid:5555");
        target.setExtraCapability("selenoid:options", "enableVNC");
        target.deactivate();

        assertEquals("Prod grid", target.getName());
        assertEquals("http://grid:5555", target.getRemoteUrl());
        assertEquals(Map.of("selenoid:options", "enableVNC"), target.getExtraCapabilities());
        assertFalse(target.isActive());
    }

    @Test
    void requiresProjectProfileAndName() {
        assertThrows(NullPointerException.class, () ->
                ExecutionTarget.create(null, DeviceProfileId.generate(), "t", null));
        assertThrows(NullPointerException.class, () ->
                ExecutionTarget.create(ProjectId.generate(), null, "t", null));
        assertThrows(IllegalArgumentException.class, () ->
                ExecutionTarget.create(ProjectId.generate(), DeviceProfileId.generate(), "  ", null));
    }

    @Test
    void reconstituteRestoresDestination() {
        Instant created = Instant.parse("2026-01-01T00:00:00Z");
        ExecutionTarget restored = ExecutionTarget.reconstitute(
                ExecutionTargetId.of("t1"),
                ProjectId.of("p1"),
                DeviceProfileId.of("dp1"),
                "Appium",
                "http://appium:4723",
                Map.of("newCommandTimeout", "60"),
                false,
                created,
                created
        );

        assertEquals("Appium", restored.getName());
        assertEquals("dp1", restored.getDeviceProfileId().value());
        assertFalse(restored.isActive());
    }
}
