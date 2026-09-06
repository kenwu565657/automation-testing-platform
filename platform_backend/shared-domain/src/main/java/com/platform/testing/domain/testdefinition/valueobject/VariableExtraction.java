package com.platform.testing.domain.testdefinition.valueobject;


public record VariableExtraction(
        String variableName,
        ExtractionSource source,
        String extractionExpression
) {
}
