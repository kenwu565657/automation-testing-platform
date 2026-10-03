package com.valdifly.domain.environment;

import com.valdifly.domain.common.AggregateRoot;
import com.valdifly.domain.environment.valueobject.EnvironmentId;
import com.valdifly.domain.project.valueobject.ProjectId;
import com.valdifly.utils.TimeUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Environment implements AggregateRoot<EnvironmentId> {

    private final EnvironmentId id;
    private final String name;
    private final ProjectId projectId;
    private final Map<String, String> variables;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static Environment create(String name, ProjectId projectId) {
        return new Environment(EnvironmentId.generate(), name, projectId);
    }

    public static Environment reconstitute(EnvironmentId id, String name,
                                           ProjectId projectId, Map<String, String> variables,
                                           boolean active, Instant createdAt, Instant updatedAt) {
        Environment env = new Environment(id, name, projectId);
        env.variables.putAll(variables);
        env.active = active;
        env.createdAt = createdAt;
        env.updatedAt = updatedAt;
        return env;
    }

    private Environment(EnvironmentId id, String name, ProjectId projectId) {
        this.id = Objects.requireNonNull(id);
        this.name = requireName(name);
        this.projectId = Objects.requireNonNull(projectId);
        this.variables = new LinkedHashMap<>();
        this.active = true;
        this.createdAt = TimeUtils.now();
        this.updatedAt = this.createdAt;
    }

    public void setVariable(String key, String value) {
        Objects.requireNonNull(key, "variable key is required");
        key = key.trim();
        if (key.isBlank()) {
            throw new IllegalArgumentException("variable key must not be blank");
        }
        this.variables.put(key, value);
        touch();
    }

    public void removeVariable(String key) {
        this.variables.remove(key);
        touch();
    }

    public void deactivate() {
        this.active = false;
        touch();
    }

    public void activate() {
        this.active = true;
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

    public EnvironmentId getId() { return id; }
    public String getName() { return name; }
    public ProjectId getProjectId() { return projectId; }
    public Map<String, String> getVariables() { return Collections.unmodifiableMap(variables); }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
