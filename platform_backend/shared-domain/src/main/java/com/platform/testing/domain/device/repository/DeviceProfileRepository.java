package com.platform.testing.domain.device.repository;

import com.platform.testing.domain.device.DeviceProfile;
import com.platform.testing.domain.device.valueobject.DeviceProfileId;
import com.platform.testing.domain.device.valueobject.PlatformType;

import java.util.List;
import java.util.Optional;

public interface DeviceProfileRepository {
    DeviceProfile save(DeviceProfile profile);

    Optional<DeviceProfile> findById(DeviceProfileId id);

    List<DeviceProfile> findAll();

    List<DeviceProfile> findByPlatform(PlatformType platform);

    void deleteById(DeviceProfileId id);
}
