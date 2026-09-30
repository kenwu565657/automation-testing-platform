package com.valdifly.domain.testdefinition.valueobject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Definition payload for {@code AuthoringStyle.DATA_DRIVEN}. Not Gherkin examples.
 * Engine expands each row into a scenario and binds {@code ${column}} values.
 */
public record DataTableSpec(String name, List<Map<String, String>> rows) {
    public DataTableSpec {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("data table name is required");
        }
        name = name.trim();
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("data table requires at least one row");
        }
        List<Map<String, String>> copy = new ArrayList<>();
        Set<String> columns = null;
        for (Map<String, String> row : rows) {
            if (row == null || row.isEmpty()) {
                throw new IllegalArgumentException("data table row cannot be empty");
            }
            Map<String, String> normalized = new LinkedHashMap<>();
            row.forEach((key, value) -> {
                if (key == null || key.isBlank()) {
                    throw new IllegalArgumentException("data table column name is required");
                }
                normalized.put(key.trim(), value);
            });
            Set<String> keys = new LinkedHashSet<>(normalized.keySet());
            if (columns == null) {
                columns = keys;
            } else if (!columns.equals(keys)) {
                throw new IllegalArgumentException("data table rows must share the same columns");
            }
            copy.add(Map.copyOf(normalized));
        }
        rows = List.copyOf(copy);
    }

    public List<String> columns() {
        return List.copyOf(rows.getFirst().keySet());
    }

    public static DataTableSpec from(List<TestParameter> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return null;
        }
        TestParameter first = parameters.getFirst();
        if (first.dataRowList() == null || first.dataRowList().isEmpty()) {
            return null;
        }
        return new DataTableSpec(first.parameterName(), first.dataRowList());
    }

    public static DataTableSpec require(List<TestParameter> parameters) {
        DataTableSpec spec = from(parameters);
        if (spec == null) {
            throw new IllegalArgumentException("DataTableSpec is required for DATA_DRIVEN");
        }
        return spec;
    }

    public List<TestParameter> toParameters() {
        return List.of(new TestParameter(name, rows));
    }
}
