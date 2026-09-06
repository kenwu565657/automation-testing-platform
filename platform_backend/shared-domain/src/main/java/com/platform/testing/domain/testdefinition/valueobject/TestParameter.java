package com.platform.testing.domain.testdefinition.valueobject;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record TestParameter(String parameterName, List<Map<String, String>> dataRowList) {
    public TestParameter {
        Objects.requireNonNull(parameterName);
        if (dataRowList == null) {
            dataRowList = List.of();
        }
    }
}
