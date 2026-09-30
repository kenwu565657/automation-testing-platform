package com.valdifly.domain.visualbaseline.repository;

import com.valdifly.domain.project.valueobject.ProjectId;
import com.valdifly.domain.visualbaseline.VisualBaseline;
import com.valdifly.domain.visualbaseline.valueobject.VisualBaselineId;

import java.util.List;
import java.util.Optional;

public interface VisualBaselineRepository {
    VisualBaseline save(VisualBaseline baseline);

    Optional<VisualBaseline> findById(VisualBaselineId id);

    Optional<VisualBaseline> findByProjectIdAndName(ProjectId projectId, String name);

    List<VisualBaseline> findByProjectId(ProjectId projectId);

    void deleteById(VisualBaselineId id);
}
