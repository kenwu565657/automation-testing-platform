package com.platform.testing.domain.testdefinition.valueobject;

import com.platform.testing.domain.pageobject.valueobject.PageElementId;

import java.util.Map;
import java.util.Objects;

public record TestStep(
        TestStepId id,
        int orderIndex,
        String name,
        ActionType actionType,
        Map<String, String> actionParameters,
        PageElementId targetElementId,
        StepAssertion assertion,
        VariableExtraction extraction,
        boolean continueOnFailure,
        int waitBeforeMs,
        int waitAfterMs
) {
    public TestStep {
        Objects.requireNonNull(id, "TestStep id is required");
        Objects.requireNonNull(actionType, "actionType is required");
        if (actionParameters == null) {
            actionParameters = Map.of();
        } else {
            actionParameters = Map.copyOf(actionParameters);
        }
    }

    public static TestStep create(
            int orderIndex,
            String name,
            ActionType actionType,
            Map<String, String> actionParameters,
            PageElementId targetElementId,
            StepAssertion assertion,
            VariableExtraction extraction,
            boolean continueOnFailure,
            int waitBeforeMs,
            int waitAfterMs
    ) {
        return new TestStep(
                TestStepId.generate(),
                orderIndex,
                name,
                actionType,
                actionParameters,
                targetElementId,
                assertion,
                extraction,
                continueOnFailure,
                waitBeforeMs,
                waitAfterMs
        );
    }
}
