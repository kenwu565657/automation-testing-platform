package com.platform.testing.domain.target;

import com.platform.testing.domain.common.AggregateRoot;
import com.platform.testing.domain.device.valueobject.DeviceProfileId;
import com.platform.testing.domain.project.valueobject.ProjectId;
import com.platform.testing.domain.target.valueobject.ExecutionTargetId;
import com.platform.testing.utils.TimeUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ExecutionTarget implements AggregateRoot<ExecutionTargetId> {

    private final ExecutionTargetId id;
    private final ProjectId projectId;
    private final DeviceProfileId deviceProfileId;
    private String name;
    private String remoteUrl;
    private final Map<String, String> extraCapabilities;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static ExecutionTarget create(
            ProjectId projectId,
            DeviceProfileId deviceProfileId,
            String name,
            String remoteUrl
    ) {
        return new ExecutionTarget(ExecutionTargetId.generate(), projectId, deviceProfileId, name, remoteUrl);
    }

    public static ExecutionTarget reconstitute(
            ExecutionTargetId id,
            ProjectId projectId,
            DeviceProfileId deviceProfileId,
            String name,
            String remoteUrl,
            Map<String, String> extraCapabilities,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        ExecutionTarget target = new ExecutionTarget(id, projectId, deviceProfileId, name, remoteUrl);
        if (extraCapabilities != null) {
            target.extraCapabilities.putAll(extraCapabilities);
        }
        target.active = active;
        target.createdAt = createdAt;
        target.updatedAt = updatedAt;
        return target;
    }

    private ExecutionTarget(
            ExecutionTargetId id,
            ProjectId projectId,
            DeviceProfileId deviceProfileId,
            String name,
            String remoteUrl
    ) {
        this.id = Objects.requireNonNull(id);
        this.projectId = Objects.requireNonNull(projectId, "projectId is required");
        this.deviceProfileId = Objects.requireNonNull(deviceProfileId, "deviceProfileId is required");
        this.name = requireName(name);
        this.remoteUrl = remoteUrl;
        this.extraCapabilities = new LinkedHashMap<>();
        this.active = true;
        this.createdAt = TimeUtils.now();
        this.updatedAt = this.createdAt;
    }

    public void rename(String newName) {
        this.name = requireName(newName);
        touch();
    }

    public void updateRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
        touch();
    }

    public void setExtraCapability(String key, String value) {
        Objects.requireNonNull(key, "capability key is required");
        this.extraCapabilities.put(key, value);
        touch();
    }

    public void deactivate() {
        this.active = false;
        touch();
    }

    private void touch() {
        this.updatedAt = TimeUtils.now();
    }

    private static String requireName(String name) {
        Objects.requireNonNull(name, "name is required");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        return name;
    }

    @Override
    public ExecutionTargetId getId() {
        return id;
    }

    public ProjectId getProjectId() {
        return projectId;
    }

    public DeviceProfileId getDeviceProfileId() {
        return deviceProfileId;
    }

    public String getName() {
        return name;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public Map<String, String> getExtraCapabilities() {
        return Collections.unmodifiableMap(extraCapabilities);
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
