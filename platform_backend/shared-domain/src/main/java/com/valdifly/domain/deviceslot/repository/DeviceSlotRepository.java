package com.valdifly.domain.deviceslot.repository;

import com.valdifly.domain.device.valueobject.DeviceProfileId;
import com.valdifly.domain.deviceslot.DeviceSlot;
import com.valdifly.domain.deviceslot.valueobject.DeviceSlotId;
import com.valdifly.domain.execution.valueobject.TestRunId;

import java.util.List;
import java.util.Optional;

public interface DeviceSlotRepository {
    DeviceSlot save(DeviceSlot slot);

    Optional<DeviceSlot> findById(DeviceSlotId id);

    List<DeviceSlot> findAll();

    List<DeviceSlot> findByDeviceProfileId(DeviceProfileId deviceProfileId);

    boolean existsByDeviceProfileId(DeviceProfileId deviceProfileId);

    Optional<DeviceSlot> findByAllocatedRunId(TestRunId runId);

    Optional<DeviceSlot> claimAvailable(DeviceProfileId deviceProfileId, TestRunId runId);

    void deleteById(DeviceSlotId id);
}
