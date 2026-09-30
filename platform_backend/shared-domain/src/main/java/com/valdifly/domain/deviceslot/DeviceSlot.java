package com.valdifly.domain.deviceslot;

import com.valdifly.domain.common.AggregateRoot;
import com.valdifly.domain.device.valueobject.DeviceProfileId;
import com.valdifly.domain.deviceslot.valueobject.DeviceSlotId;
import com.valdifly.domain.deviceslot.valueobject.DeviceSlotStatus;
import com.valdifly.domain.execution.valueobject.TestRunId;
import com.valdifly.utils.TimeUtils;

import java.time.Instant;
import java.util.Objects;

/**
 * Physical farm inventory unit. {@link com.valdifly.domain.device.DeviceProfile}
 * stays a reusable preset; this slot owns the live Selenium/Appium URL that a run can lease.
 */
public class DeviceSlot implements AggregateRoot<DeviceSlotId> {

    private final DeviceSlotId id;
    private String name;
    private final DeviceProfileId deviceProfileId;
    private String remoteUrl;
    private String udid;
    private DeviceSlotStatus status;
    private TestRunId allocatedRunId;
    private Instant allocatedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static DeviceSlot create(String name, DeviceProfileId deviceProfileId, String remoteUrl) {
        return new DeviceSlot(DeviceSlotId.generate(), name, deviceProfileId, remoteUrl);
    }

    public static DeviceSlot reconstitute(
            DeviceSlotId id,
            String name,
            DeviceProfileId deviceProfileId,
            String remoteUrl,
            String udid,
            DeviceSlotStatus status,
            TestRunId allocatedRunId,
            Instant allocatedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        DeviceSlot slot = new DeviceSlot(id, name, deviceProfileId, remoteUrl);
        slot.udid = blankToNull(udid);
        slot.status = Objects.requireNonNull(status, "status is required");
        slot.allocatedRunId = allocatedRunId;
        slot.allocatedAt = allocatedAt;
        slot.createdAt = createdAt;
        slot.updatedAt = updatedAt;
        return slot;
    }

    private DeviceSlot(DeviceSlotId id, String name, DeviceProfileId deviceProfileId, String remoteUrl) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.name = requireName(name);
        this.deviceProfileId = Objects.requireNonNull(deviceProfileId, "deviceProfileId is required");
        this.remoteUrl = requireRemoteUrl(remoteUrl);
        this.status = DeviceSlotStatus.AVAILABLE;
        this.createdAt = TimeUtils.now();
        this.updatedAt = this.createdAt;
    }

    public void rename(String name) {
        this.name = requireName(name);
        touch();
    }

    public void updateRemoteUrl(String remoteUrl) {
        this.remoteUrl = requireRemoteUrl(remoteUrl);
        touch();
    }

    public void describeUdid(String udid) {
        this.udid = blankToNull(udid);
        touch();
    }

    public void allocate(TestRunId runId) {
        Objects.requireNonNull(runId, "runId is required");
        if (status != DeviceSlotStatus.AVAILABLE) {
            throw new IllegalStateException("DeviceSlot is not available: " + status);
        }
        this.status = DeviceSlotStatus.ALLOCATED;
        this.allocatedRunId = runId;
        this.allocatedAt = TimeUtils.now();
        touch();
    }

    public void release() {
        if (status == DeviceSlotStatus.OFFLINE) {
            throw new IllegalStateException("Cannot release an offline slot");
        }
        if (status == DeviceSlotStatus.AVAILABLE) {
            return;
        }
        this.status = DeviceSlotStatus.AVAILABLE;
        this.allocatedRunId = null;
        this.allocatedAt = null;
        touch();
    }

    public void takeOffline() {
        if (status == DeviceSlotStatus.ALLOCATED) {
            throw new IllegalStateException("Cannot take an allocated slot offline");
        }
        this.status = DeviceSlotStatus.OFFLINE;
        this.allocatedRunId = null;
        this.allocatedAt = null;
        touch();
    }

    public void bringOnline() {
        if (status != DeviceSlotStatus.OFFLINE) {
            throw new IllegalStateException("Only an offline slot can be brought online");
        }
        this.status = DeviceSlotStatus.AVAILABLE;
        touch();
    }

    private void touch() {
        this.updatedAt = TimeUtils.now();
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        return name.trim();
    }

    private static String requireRemoteUrl(String remoteUrl) {
        if (remoteUrl == null || remoteUrl.isBlank()) {
            throw new IllegalArgumentException("remoteUrl is required");
        }
        return remoteUrl.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public DeviceSlotId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DeviceProfileId getDeviceProfileId() {
        return deviceProfileId;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public String getUdid() {
        return udid;
    }

    public DeviceSlotStatus getStatus() {
        return status;
    }

    public TestRunId getAllocatedRunId() {
        return allocatedRunId;
    }

    public Instant getAllocatedAt() {
        return allocatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
