package com.valdifly.domain.environment.repository;

import com.valdifly.domain.environment.Environment;
import com.valdifly.domain.environment.valueobject.EnvironmentId;
import com.valdifly.domain.project.valueobject.ProjectId;

import java.util.List;
import java.util.Optional;

public interface EnvironmentRepository {
    Environment save(Environment environment);
    Optional<Environment> findById(EnvironmentId id);
    List<Environment> findByProjectId(ProjectId projectId);
    void deleteById(EnvironmentId id);
}
