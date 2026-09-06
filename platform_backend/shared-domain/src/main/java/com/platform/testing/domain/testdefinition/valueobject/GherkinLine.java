package com.platform.testing.domain.testdefinition.valueobject;

import java.util.Objects;

public record GherkinLine(
        int orderIndex,
        GherkinKeyword keyword,
        String stepText,
        boolean background
) {
    public GherkinLine {
        Objects.requireNonNull(keyword, "keyword is required");
        Objects.requireNonNull(stepText, "stepText is required");
        if (stepText.isBlank()) {
            throw new IllegalArgumentException("stepText cannot be blank");
        }
    }
}
