package com.platform.testing.domain.project;

import com.platform.testing.domain.project.valueobject.ProjectId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectTest {

    @Test
    void createRenameDeactivate() {
        Project project = Project.create("Shop", "e2e");
        assertTrue(project.isActive());
        assertEquals("e2e", project.getDescription());

        project.rename("Store");
        assertEquals("Store", project.getName());

        project.deactivate();
        assertFalse(project.isActive());
    }

    @Test
    void reconstitutePreservesState() {
        ProjectId id = ProjectId.generate();
        Instant created = Instant.parse("2026-03-01T08:00:00Z");
        Project project = Project.reconstitute(id, "Shop", "d", false, created, created.plusSeconds(86400));

        assertEquals(id, project.getId());
        assertFalse(project.isActive());
        assertEquals(created, project.getCreatedAt());
    }

    @Test
    void renameRejectsNull() {
        Project project = Project.create("Shop", null);
        assertThrows(NullPointerException.class, () -> project.rename(null));
    }
}
