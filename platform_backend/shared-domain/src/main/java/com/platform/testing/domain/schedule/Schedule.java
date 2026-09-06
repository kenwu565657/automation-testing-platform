package com.platform.testing.domain.schedule;

import com.platform.testing.domain.common.Priority;
import com.platform.testing.domain.testsuite.valueobject.TestSuiteId;
import com.platform.testing.domain.common.AggregateRoot;
import com.platform.testing.domain.environment.valueobject.EnvironmentId;
import com.platform.testing.domain.projectmembership.valueobject.ResourceArn;
import com.platform.testing.domain.project.valueobject.ProjectId;
import com.platform.testing.domain.schedule.valueobject.ScheduleId;
import com.platform.testing.domain.target.valueobject.ExecutionTargetId;
import com.platform.testing.domain.user.valueobject.UserId;
import com.platform.testing.utils.TimeUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

public class Schedule implements AggregateRoot<ScheduleId> {

    private final ScheduleId id;
    private final ProjectId projectId;
    private String name;
    private String cron;
    private ZoneId zoneId;
    private final TestSuiteId testSuiteId;
    private final EnvironmentId environmentId;
    private final ExecutionTargetId executionTargetId;
    private Priority priority;
    private boolean active;
    private Instant lastFiredAt;
    private Instant createdAt;
    private Instant updatedAt;
    private final UserId createdBy;

    public static Schedule create(
            ProjectId projectId,
            String name,
            String cron,
            ZoneId zoneId,
            TestSuiteId testSuiteId,
            EnvironmentId environmentId,
            ExecutionTargetId executionTargetId,
            Priority priority,
            UserId createdBy
    ) {
        return new Schedule(
                ScheduleId.generate(),
                projectId,
                name,
                cron,
                zoneId,
                testSuiteId,
                environmentId,
                executionTargetId,
                priority,
                createdBy
        );
    }

    public static Schedule reconstitute(
            ScheduleId id,
            ProjectId projectId,
            String name,
            String cron,
            ZoneId zoneId,
            TestSuiteId testSuiteId,
            EnvironmentId environmentId,
            ExecutionTargetId executionTargetId,
            Priority priority,
            boolean active,
            Instant lastFiredAt,
            Instant createdAt,
            Instant updatedAt,
            UserId createdBy
    ) {
        Schedule schedule = new Schedule(
                id, projectId, name, cron, zoneId, testSuiteId, environmentId, executionTargetId, priority, createdBy
        );
        schedule.active = active;
        schedule.lastFiredAt = lastFiredAt;
        schedule.createdAt = createdAt;
        schedule.updatedAt = updatedAt;
        return schedule;
    }

    private Schedule(
            ScheduleId id,
            ProjectId projectId,
            String name,
            String cron,
            ZoneId zoneId,
            TestSuiteId testSuiteId,
            EnvironmentId environmentId,
            ExecutionTargetId executionTargetId,
            Priority priority,
            UserId createdBy
    ) {
        this.id = Objects.requireNonNull(id);
        this.projectId = Objects.requireNonNull(projectId, "projectId is required");
        this.name = requireName(name);
        this.cron = requireCron(cron);
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId is required");
        this.testSuiteId = Objects.requireNonNull(testSuiteId, "testSuiteId is required");
        this.environmentId = Objects.requireNonNull(environmentId, "environmentId is required");
        this.executionTargetId = Objects.requireNonNull(executionTargetId, "executionTargetId is required");
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy is required");
        this.active = true;
        this.createdAt = TimeUtils.now();
        this.updatedAt = this.createdAt;
    }

    public void rename(String name) {
        this.name = requireName(name);
        touch();
    }

    public void updateCron(String cron, ZoneId zoneId) {
        this.cron = requireCron(cron);
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId is required");
        touch();
    }

    public void changePriority(Priority priority) {
        this.priority = priority != null ? priority : Priority.MEDIUM;
        touch();
    }

    public void pause() {
        this.active = false;
        touch();
    }

    public void resume() {
        this.active = true;
        touch();
    }

    public void recordFired(Instant firedAt) {
        this.lastFiredAt = Objects.requireNonNull(firedAt, "firedAt is required");
        touch();
    }

    public ResourceArn resourceArn() {
        return ResourceArn.of(projectId, "schedule", id.value());
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

    private static String requireCron(String cron) {
        Objects.requireNonNull(cron, "cron is required");
        String[] fields = cron.trim().split("\\s+");
        if (fields.length < 6 || fields.length > 7) {
            throw new IllegalArgumentException("cron must be a Quartz expression with 6 or 7 fields");
        }
        return cron.trim();
    }

    @Override
    public ScheduleId getId() {
        return id;
    }

    public ProjectId getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public String getCron() {
        return cron;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public TestSuiteId getTestSuiteId() {
        return testSuiteId;
    }

    public EnvironmentId getEnvironmentId() {
        return environmentId;
    }

    public ExecutionTargetId getExecutionTargetId() {
        return executionTargetId;
    }

    public Priority getPriority() {
        return priority;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getLastFiredAt() {
        return lastFiredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public UserId getCreatedBy() {
        return createdBy;
    }
}
