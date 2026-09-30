package com.valdifly.domain.testdefinition.valueobject;


public record StepAssertion(
        AssertionType assertionType,
        ComparisonOperator operator,
        String expectedValue,
        String actualValueSource
) {
}
