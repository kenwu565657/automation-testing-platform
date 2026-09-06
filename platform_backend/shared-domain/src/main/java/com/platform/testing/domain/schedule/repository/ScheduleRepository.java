package com.platform.testing.domain.schedule.repository;

import com.platform.testing.domain.project.valueobject.ProjectId;
import com.platform.testing.domain.schedule.Schedule;
import com.platform.testing.domain.schedule.valueobject.ScheduleId;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository {
    Schedule save(Schedule schedule);

    Optional<Schedule> findById(ScheduleId id);

    List<Schedule> findByProjectId(ProjectId projectId);

    List<Schedule> findActive();

    void deleteById(ScheduleId id);
}
