package com.platform.testing.domain.environment;

import com.platform.testing.domain.common.AggregateRoot;
import com.platform.testing.domain.environment.valueobject.EnvironmentId;
import com.platform.testing.domain.project.valueobject.ProjectId;
import com.platform.testing.utils.TimeUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Environment implements AggregateRoot<EnvironmentId> {

    private final EnvironmentId id;
    private final String name;
    private String baseUrl;
    private final ProjectId projectId;
    private final Map<String, String> variables;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static Environment create(String name, String baseUrl, ProjectId projectId) {
        return new Environment(EnvironmentId.generate(), name, baseUrl, projectId);
    }

    public static Environment reconstitute(EnvironmentId id, String name, String baseUrl,
                                           ProjectId projectId, Map<String, String> variables,
                                           boolean active, Instant createdAt, Instant updatedAt) {
        Environment env = new Environment(id, name, baseUrl, projectId);
        env.variables.putAll(variables);
        env.active = active;
        env.createdAt = createdAt;
        env.updatedAt = updatedAt;
        return env;
    }

    private Environment(EnvironmentId id, String name, String baseUrl, ProjectId projectId) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.baseUrl = baseUrl;
        this.projectId = Objects.requireNonNull(projectId);
        this.variables = new LinkedHashMap<>();
        this.active = true;
        this.createdAt = TimeUtils.now();
        this.updatedAt = this.createdAt;
    }

    public void setVariable(String key, String value) {
        this.variables.put(key, value);
        touch();
    }

    public void removeVariable(String key) {
        this.variables.remove(key);
        touch();
    }

    public void updateBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        touch();
    }

    public void deactivate() {
        this.active = false;
        touch();
    }

    private void touch() {
        this.updatedAt = TimeUtils.now();
    }

    public EnvironmentId getId() { return id; }
    public String getName() { return name; }
    public String getBaseUrl() { return baseUrl; }
    public ProjectId getProjectId() { return projectId; }
    public Map<String, String> getVariables() { return Collections.unmodifiableMap(variables); }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
