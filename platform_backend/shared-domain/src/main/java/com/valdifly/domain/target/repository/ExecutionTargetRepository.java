package com.valdifly.domain.target.repository;

import com.valdifly.domain.project.valueobject.ProjectId;
import com.valdifly.domain.target.ExecutionTarget;
import com.valdifly.domain.target.valueobject.ExecutionTargetId;

import java.util.List;
import java.util.Optional;

public interface ExecutionTargetRepository {
    ExecutionTarget save(ExecutionTarget target);

    Optional<ExecutionTarget> findById(ExecutionTargetId id);

    List<ExecutionTarget> findByProjectId(ProjectId projectId);

    void deleteById(ExecutionTargetId id);
}
