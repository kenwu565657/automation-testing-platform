package com.valdifly.domain.schedule;

import com.valdifly.domain.common.Priority;
import com.valdifly.domain.testsuite.valueobject.TestSuiteId;
import com.valdifly.domain.environment.valueobject.EnvironmentId;
import com.valdifly.domain.projectmembership.valueobject.ResourceArn;
import com.valdifly.domain.project.valueobject.ProjectId;
import com.valdifly.domain.target.valueobject.ExecutionTargetId;
import com.valdifly.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScheduleTest {

    @Test
    void createBindsSuiteEnvironmentAndTarget() {
        ProjectId projectId = ProjectId.of("proj-1");
        TestSuiteId suiteId = TestSuiteId.generate();
        EnvironmentId environmentId = EnvironmentId.generate();
        ExecutionTargetId targetId = ExecutionTargetId.generate();

        Schedule schedule = Schedule.create(
                projectId, "Nightly", "0 0 9 * * ?", ZoneId.of("Asia/Taipei"),
                suiteId, environmentId, targetId, Priority.HIGH, UserId.of("qa")
        );

        assertEquals(suiteId, schedule.getTestSuiteId());
        assertEquals(environmentId, schedule.getEnvironmentId());
        assertEquals(targetId, schedule.getExecutionTargetId());
        assertEquals(ZoneId.of("Asia/Taipei"), schedule.getZoneId());
        assertTrue(schedule.isActive());
        assertEquals(ResourceArn.of(projectId, "schedule", schedule.getId().value()), schedule.resourceArn());
    }

    @Test
    void cronMustBeQuartzForm() {
        assertThrows(IllegalArgumentException.class, () -> Schedule.create(
                ProjectId.generate(), "Bad", "0 9 * * *", ZoneId.of("UTC"),
                TestSuiteId.generate(), EnvironmentId.generate(), ExecutionTargetId.generate(),
                null, UserId.of("qa")
        ));
    }

    @Test
    void pauseResumeAndRecordFired() {
        Schedule schedule = newSchedule();
        Instant fired = Instant.parse("2026-01-01T01:00:00Z");
        schedule.recordFired(fired);
        schedule.pause();

        assertEquals(fired, schedule.getLastFiredAt());
        assertFalse(schedule.isActive());

        schedule.resume();
        assertTrue(schedule.isActive());
    }

    @Test
    void updateCronAndZone() {
        Schedule schedule = newSchedule();
        schedule.updateCron("0 30 18 * * ?", ZoneId.of("UTC"));
        assertEquals("0 30 18 * * ?", schedule.getCron());
        assertEquals(ZoneId.of("UTC"), schedule.getZoneId());
    }

    private static Schedule newSchedule() {
        return Schedule.create(
                ProjectId.generate(), "Nightly", "0 0 9 * * ?", ZoneId.of("Asia/Taipei"),
                TestSuiteId.generate(), EnvironmentId.generate(), ExecutionTargetId.generate(),
                Priority.MEDIUM, UserId.of("qa")
        );
    }
}
