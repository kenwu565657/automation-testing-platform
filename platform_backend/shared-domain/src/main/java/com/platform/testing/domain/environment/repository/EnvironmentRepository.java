package com.platform.testing.domain.environment.repository;

import com.platform.testing.domain.environment.Environment;
import com.platform.testing.domain.environment.valueobject.EnvironmentId;
import com.platform.testing.domain.project.valueobject.ProjectId;

import java.util.List;
import java.util.Optional;

public interface EnvironmentRepository {
    Environment save(Environment environment);
    Optional<Environment> findById(EnvironmentId id);
    List<Environment> findByProjectId(ProjectId projectId);
    void deleteById(EnvironmentId id);
}
