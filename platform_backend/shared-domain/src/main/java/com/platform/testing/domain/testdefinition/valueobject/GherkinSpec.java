package com.platform.testing.domain.testdefinition.valueobject;

import java.util.List;
import java.util.Objects;

public record GherkinSpec(
        String featureTitle,
        String scenarioTitle,
        List<GherkinLine> lines
) {
    public GherkinSpec {
        Objects.requireNonNull(featureTitle, "featureTitle is required");
        Objects.requireNonNull(scenarioTitle, "scenarioTitle is required");
        if (featureTitle.isBlank()) {
            throw new IllegalArgumentException("featureTitle cannot be blank");
        }
        if (scenarioTitle.isBlank()) {
            throw new IllegalArgumentException("scenarioTitle cannot be blank");
        }
        if (lines == null) {
            lines = List.of();
        } else {
            lines = List.copyOf(lines);
        }
    }

    public List<GherkinLine> backgroundLines() {
        return lines.stream().filter(GherkinLine::background).toList();
    }

    public List<GherkinLine> scenarioLines() {
        return lines.stream().filter(line -> !line.background()).toList();
    }
}
