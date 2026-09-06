package com.platform.testing.domain.testdefinition;

import com.platform.testing.domain.testdefinition.valueobject.ActionType;
import com.platform.testing.domain.testcase.valueobject.AuthoringStyle;
import com.platform.testing.domain.testdefinition.valueobject.GherkinKeyword;
import com.platform.testing.domain.testdefinition.valueobject.GherkinLine;
import com.platform.testing.domain.testdefinition.valueobject.GherkinSpec;
import com.platform.testing.domain.testcase.valueobject.TestCaseId;
import com.platform.testing.domain.testdefinition.valueobject.TestStep;
import com.platform.testing.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestDefinitionTest {

    @Test
    void simpleStyleRejectsGherkinSpec() {
        assertThrows(IllegalArgumentException.class, () -> TestDefinition.create(
                TestCaseId.generate(), 1, AuthoringStyle.SIMPLE,
                List.of(step(1)),
                new GherkinSpec("Auth", "Login", List.of()),
                List.of(),
                UserId.of("qa")
        ));
    }

    @Test
    void gherkinStyleRequiresSpec() {
        assertThrows(NullPointerException.class, () -> TestDefinition.create(
                TestCaseId.generate(), 1, AuthoringStyle.GHERKIN,
                List.of(step(1)),
                null,
                List.of(),
                UserId.of("qa")
        ));
    }

    @Test
    void createSimpleDefinition() {
        TestCaseId caseId = TestCaseId.generate();
        TestDefinition definition = TestDefinition.create(
                caseId, 1, AuthoringStyle.SIMPLE,
                List.of(step(1), step(2)),
                null,
                List.of(),
                UserId.of("qa")
        );

        assertEquals(caseId, definition.getTestCaseId());
        assertEquals(1, definition.getVersion());
        assertEquals(2, definition.stepCount());
        assertNull(definition.getGherkinSpec());
        assertThrows(UnsupportedOperationException.class, () -> definition.getSteps().add(step(3)));
    }

    @Test
    void rejectsDuplicateOrderAndEmptySteps() {
        assertThrows(IllegalArgumentException.class, () -> TestDefinition.create(
                TestCaseId.generate(), 1, AuthoringStyle.SIMPLE,
                List.of(step(1), step(1)),
                null, List.of(), UserId.of("qa")
        ));
        assertThrows(IllegalArgumentException.class, () -> TestDefinition.create(
                TestCaseId.generate(), 1, AuthoringStyle.SIMPLE,
                List.of(),
                null, List.of(), UserId.of("qa")
        ));
        assertThrows(IllegalArgumentException.class, () -> TestDefinition.create(
                TestCaseId.generate(), 0, AuthoringStyle.SIMPLE,
                List.of(step(1)),
                null, List.of(), UserId.of("qa")
        ));
    }

    @Test
    void gherkinSpecKeepsBackgroundSplit() {
        GherkinSpec spec = new GherkinSpec(
                "Auth",
                "User logs in",
                List.of(
                        new GherkinLine(1, GherkinKeyword.GIVEN, "open login", true),
                        new GherkinLine(2, GherkinKeyword.WHEN, "click login", false)
                )
        );
        TestDefinition definition = TestDefinition.create(
                TestCaseId.generate(), 1, AuthoringStyle.GHERKIN,
                List.of(step(1), step(2)),
                spec,
                List.of(),
                UserId.of("qa")
        );

        assertEquals(1, definition.getGherkinSpec().backgroundLines().size());
        assertEquals(1, definition.getGherkinSpec().scenarioLines().size());
    }

    static TestStep step(int order) {
        return TestStep.create(
                order, "step-" + order, ActionType.CLICK, Map.of(), null,
                null, null, false, 0, 0
        );
    }
}
