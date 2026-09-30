package com.valdifly.domain.project.repository;

import com.valdifly.domain.organization.valueobject.OrganizationId;
import com.valdifly.domain.project.Project;
import com.valdifly.domain.project.valueobject.ProjectId;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
    Project save(Project project);
    Optional<Project> findById(ProjectId id);
    List<Project> findAll();
    List<Project> findByOrganizationId(OrganizationId organizationId);
    void deleteById(ProjectId id);
}
