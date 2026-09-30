package com.valdifly.domain.visualbaseline;

import com.valdifly.domain.project.valueobject.ProjectId;
import com.valdifly.domain.user.valueobject.UserId;
import com.valdifly.domain.visualbaseline.valueobject.VisualBaselineId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VisualBaselineTest {

    @Test
    void createRejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> VisualBaseline.create(
                VisualBaselineId.generate(),
                ProjectId.generate(),
                "  ",
                "baselines/p/b/baseline.png",
                UserId.of("qa")
        ));
    }

    @Test
    void replaceImageUpdatesKey() {
        VisualBaseline baseline = VisualBaseline.create(
                VisualBaselineId.generate(),
                ProjectId.generate(),
                "login",
                "baselines/p/b/baseline.png",
                UserId.of("qa")
        );
        baseline.replaceImage("baselines/p/b2/baseline.png");
        assertEquals("baselines/p/b2/baseline.png", baseline.getImageKey());
    }
}
