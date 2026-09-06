package com.platform.testing.domain.project.repository;

import com.platform.testing.domain.project.Project;
import com.platform.testing.domain.project.valueobject.ProjectId;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
    Project save(Project project);
    Optional<Project> findById(ProjectId id);
    List<Project> findAll();
    void deleteById(ProjectId id);
}
