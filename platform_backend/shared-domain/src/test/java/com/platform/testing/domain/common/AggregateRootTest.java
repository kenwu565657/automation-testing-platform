package com.platform.testing.domain.common;

import com.platform.testing.domain.project.Project;
import com.platform.testing.domain.project.valueobject.ProjectId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AggregateRootTest {

    @Test
    void idIsAvailableThroughTheInterface() {
        AggregateRoot<ProjectId> project = Project.create("Shop", null);
        assertNotNull(project.getId());
        assertEquals(project.getId(), ((Project) project).getId());
    }
}
