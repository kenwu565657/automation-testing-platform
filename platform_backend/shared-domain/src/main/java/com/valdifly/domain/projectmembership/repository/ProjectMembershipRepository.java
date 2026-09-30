package com.valdifly.domain.projectmembership.repository;

import com.valdifly.domain.projectmembership.ProjectMembership;
import com.valdifly.domain.projectmembership.valueobject.ProjectMembershipId;
import com.valdifly.domain.user.valueobject.UserId;
import com.valdifly.domain.project.valueobject.ProjectId;

import java.util.List;
import java.util.Optional;

public interface ProjectMembershipRepository {
    ProjectMembership save(ProjectMembership membership);

    Optional<ProjectMembership> findById(ProjectMembershipId id);

    Optional<ProjectMembership> findByUserIdAndProjectId(UserId userId, ProjectId projectId);

    List<ProjectMembership> findByUserId(UserId userId);

    List<ProjectMembership> findByProjectId(ProjectId projectId);

    void deleteById(ProjectMembershipId id);
}
