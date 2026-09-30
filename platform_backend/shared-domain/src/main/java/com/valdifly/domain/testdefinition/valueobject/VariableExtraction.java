package com.valdifly.domain.testdefinition.valueobject;


public record VariableExtraction(
        String variableName,
        ExtractionSource source,
        String extractionExpression
) {
}
