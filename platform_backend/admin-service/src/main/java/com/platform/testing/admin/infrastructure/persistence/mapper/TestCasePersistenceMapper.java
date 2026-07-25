package com.platform.testing.admin.infrastructure.persistence.mapper;

import com.platform.testing.admin.infrastructure.persistence.jpa.entity.TestCaseJpaEntity;
import com.platform.testing.admin.infrastructure.persistence.jpa.entity.TestStepJpaEntity;
import com.platform.testing.domain.constant.ActionType;
import com.platform.testing.domain.constant.AssertionType;
import com.platform.testing.domain.constant.ComparisonOperator;
import com.platform.testing.domain.constant.ExtractionSource;
import com.platform.testing.domain.constant.GherkinKeyword;
import com.platform.testing.domain.constant.Priority;
import com.platform.testing.domain.constant.TestType;
import com.platform.testing.domain.testcase.GherkinStep;
import com.platform.testing.domain.testcase.StepAssertion;
import com.platform.testing.domain.testcase.TestCase;
import com.platform.testing.domain.testcase.TestCaseId;
import com.platform.testing.domain.testcase.TestParameter;
import com.platform.testing.domain.testcase.VariableExtraction;
import com.platform.testing.domain.testsuite.TestSuiteId;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
public class TestCasePersistenceMapper {

    public TestCaseJpaEntity toJpa(TestCase domain) {
        TestCaseJpaEntity jpa = TestCaseJpaEntity.builder()
                .id(domain.getId().value())
                .name(domain.getName())
                .description(domain.getDescription())
                .featureTitle(domain.getFeatureTitle())
                .scenarioTitle(domain.getScenarioTitle())
                .testType(domain.getTestType().name())
                .priority(domain.getPriority().name())
                .testSuiteId(domain.getTestSuiteId() != null ? domain.getTestSuiteId().value() : null)
                .active(domain.isActive())
                .timeoutSeconds(domain.getTimeoutSeconds())
                .retryCount(domain.getRetryCount())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .createdBy(domain.getCreatedBy())
                .tags(new HashSet<>(domain.getTags()))
                .build();

        domain.getSteps().forEach(step -> {
            TestStepJpaEntity stepJpa = TestStepJpaEntity.builder()
                    .orderIndex(step.orderIndex())
                    .name(step.name())
                    .gherkinKeyword(step.keyword().name())
                    .gherkinStepText(step.stepText())
                    .actionType(step.actionType().name())
                    .actionParameters(step.actionParameters())
                    .targetElementId(step.targetElementId())
                    .assertionType(step.assertion() != null ? step.assertion().assertionType().name() : null)
                    .assertionOperator(step.assertion() != null ? step.assertion().operator().name() : null)
                    .expectedValue(step.assertion() != null ? step.assertion().expectedValue() : null)
                    .actualValueSource(step.assertion() != null ? step.assertion().actualValueSource() : null)
                    .extractionVarName(step.variableExtraction() != null ? step.variableExtraction().variableName() : null)
                    .extractionSource(step.variableExtraction() != null ? step.variableExtraction().source().name() : null)
                    .extractionExpression(step.variableExtraction() != null ? step.variableExtraction().extractionExpression() : null)
                    .background(step.background())
                    .continueOnFailure(step.continueOnFailure())
                    .waitBeforeMs(step.waitBeforeMs())
                    .waitAfterMs(step.waitAfterMs())
                    .build();
            jpa.addStep(stepJpa);
        });

        return jpa;
    }

    public TestCase toDomain(TestCaseJpaEntity jpa) {
        List<GherkinStep> steps = jpa.getSteps().stream().map(s -> {
            StepAssertion assertion = null;
            if (s.getAssertionType() != null) {
                assertion = new StepAssertion(
                        AssertionType.valueOf(s.getAssertionType()),
                        ComparisonOperator.valueOf(s.getAssertionOperator()),
                        s.getExpectedValue(),
                        s.getActualValueSource()
                );
            }

            VariableExtraction extraction = null;
            if (s.getExtractionVarName() != null) {
                extraction = new VariableExtraction(
                        s.getExtractionVarName(),
                        ExtractionSource.valueOf(s.getExtractionSource()),
                        s.getExtractionExpression()
                );
            }

            return new GherkinStep(
                    s.getOrderIndex(), s.getName(),
                    GherkinKeyword.valueOf(s.getGherkinKeyword()),
                    s.getGherkinStepText(),
                    ActionType.valueOf(s.getActionType()),
                    s.getActionParameters() != null ? s.getActionParameters() : Map.of(),
                    s.getTargetElementId(),
                    assertion, extraction,
                    s.isBackground(), s.isContinueOnFailure(),
                    s.getWaitBeforeMs(), s.getWaitAfterMs()
            );
        }).toList();

        return TestCase.reconstitute(
                TestCaseId.of(jpa.getId()),
                jpa.getName(), jpa.getDescription(),
                jpa.getFeatureTitle(), jpa.getScenarioTitle(),
                TestType.valueOf(jpa.getTestType()),
                Priority.valueOf(jpa.getPriority()),
                jpa.getTestSuiteId() != null ? TestSuiteId.of(jpa.getTestSuiteId()) : null,
                steps,
                jpa.getParameters().stream().map(p -> new TestParameter(
                        p.getParameterName(), p.getDataRows()
                )).toList(),
                jpa.getTags(),
                jpa.isActive(), jpa.getTimeoutSeconds(), jpa.getRetryCount(),
                jpa.getCreatedAt(), jpa.getUpdatedAt(), jpa.getCreatedBy()
        );
    }
}
