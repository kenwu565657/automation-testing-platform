package com.platform.testing.domain.testdefinition;

import com.platform.testing.domain.testcase.valueobject.AuthoringStyle;
import com.platform.testing.domain.testdefinition.valueobject.GherkinSpec;
import com.platform.testing.domain.testcase.valueobject.TestCaseId;
import com.platform.testing.domain.testdefinition.valueobject.TestDefinitionId;
import com.platform.testing.domain.testdefinition.valueobject.TestParameter;
import com.platform.testing.domain.testdefinition.valueobject.TestStep;
import com.platform.testing.domain.common.AggregateRoot;
import com.platform.testing.domain.user.valueobject.UserId;
import com.platform.testing.utils.TimeUtils;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TestDefinition implements AggregateRoot<TestDefinitionId> {

    private final TestDefinitionId id;
    private final TestCaseId testCaseId;
    private final int version;
    private final AuthoringStyle authoringStyle;
    private final List<TestStep> steps;
    private final GherkinSpec gherkinSpec;
    private final List<TestParameter> parameters;
    private final Instant createdAt;
    private final UserId createdBy;

    public static TestDefinition create(
            TestCaseId testCaseId,
            int version,
            AuthoringStyle authoringStyle,
            List<TestStep> steps,
            GherkinSpec gherkinSpec,
            List<TestParameter> parameters,
            UserId createdBy
    ) {
        return new TestDefinition(
                TestDefinitionId.generate(),
                testCaseId,
                version,
                authoringStyle,
                steps,
                gherkinSpec,
                parameters,
                TimeUtils.now(),
                Objects.requireNonNull(createdBy, "createdBy is required")
        );
    }

    public static TestDefinition reconstitute(
            TestDefinitionId id,
            TestCaseId testCaseId,
            int version,
            AuthoringStyle authoringStyle,
            List<TestStep> steps,
            GherkinSpec gherkinSpec,
            List<TestParameter> parameters,
            Instant createdAt,
            UserId createdBy
    ) {
        return new TestDefinition(
                id, testCaseId, version, authoringStyle, steps, gherkinSpec, parameters, createdAt, createdBy
        );
    }

    private TestDefinition(
            TestDefinitionId id,
            TestCaseId testCaseId,
            int version,
            AuthoringStyle authoringStyle,
            List<TestStep> steps,
            GherkinSpec gherkinSpec,
            List<TestParameter> parameters,
            Instant createdAt,
            UserId createdBy
    ) {
        this.id = Objects.requireNonNull(id);
        this.testCaseId = Objects.requireNonNull(testCaseId);
        this.authoringStyle = Objects.requireNonNull(authoringStyle);
        if (version < 1) {
            throw new IllegalArgumentException("definition version must be >= 1");
        }
        this.version = version;
        this.steps = List.copyOf(requireUniqueOrder(steps));
        if (authoringStyle == AuthoringStyle.GHERKIN) {
            this.gherkinSpec = Objects.requireNonNull(gherkinSpec, "GherkinSpec is required for GHERKIN");
        } else if (gherkinSpec != null) {
            throw new IllegalArgumentException("GherkinSpec is only allowed for GHERKIN");
        } else {
            this.gherkinSpec = null;
        }
        this.parameters = parameters == null ? List.of() : List.copyOf(parameters);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy is required");
    }

    private static List<TestStep> requireUniqueOrder(List<TestStep> steps) {
        Objects.requireNonNull(steps, "steps are required");
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("TestDefinition requires at least one step");
        }
        Set<Integer> seen = new HashSet<>();
        for (TestStep step : steps) {
            Objects.requireNonNull(step, "step is required");
            if (!seen.add(step.orderIndex())) {
                throw new IllegalArgumentException("Duplicate step orderIndex: " + step.orderIndex());
            }
        }
        return steps;
    }

    public int stepCount() {
        return steps.size();
    }

    @Override
    public TestDefinitionId getId() {
        return id;
    }

    public TestCaseId getTestCaseId() {
        return testCaseId;
    }

    public int getVersion() {
        return version;
    }

    public AuthoringStyle getAuthoringStyle() {
        return authoringStyle;
    }

    public List<TestStep> getSteps() {
        return steps;
    }

    public GherkinSpec getGherkinSpec() {
        return gherkinSpec;
    }

    public List<TestParameter> getParameters() {
        return parameters;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UserId getCreatedBy() {
        return createdBy;
    }
}
