package com.platform.testing.domain.testdefinition.repository;

import com.platform.testing.domain.testdefinition.TestDefinition;
import com.platform.testing.domain.testcase.valueobject.TestCaseId;
import com.platform.testing.domain.testdefinition.valueobject.TestDefinitionId;

import java.util.List;
import java.util.Optional;

public interface TestDefinitionRepository {
    TestDefinition save(TestDefinition definition);

    Optional<TestDefinition> findById(TestDefinitionId id);

    Optional<TestDefinition> findByTestCaseIdAndVersion(TestCaseId testCaseId, int version);

    Optional<TestDefinition> findLatestByTestCaseId(TestCaseId testCaseId);

    List<TestDefinition> findByTestCaseId(TestCaseId testCaseId);
}
