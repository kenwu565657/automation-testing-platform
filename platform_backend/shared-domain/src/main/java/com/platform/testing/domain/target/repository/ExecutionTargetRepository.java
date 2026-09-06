package com.platform.testing.domain.target.repository;

import com.platform.testing.domain.project.valueobject.ProjectId;
import com.platform.testing.domain.target.ExecutionTarget;
import com.platform.testing.domain.target.valueobject.ExecutionTargetId;

import java.util.List;
import java.util.Optional;

public interface ExecutionTargetRepository {
    ExecutionTarget save(ExecutionTarget target);

    Optional<ExecutionTarget> findById(ExecutionTargetId id);

    List<ExecutionTarget> findByProjectId(ProjectId projectId);

    void deleteById(ExecutionTargetId id);
}
