package com.platform.testing.domain.testcase.repository;

import com.platform.testing.domain.testcase.TestCase;
import com.platform.testing.domain.testcase.valueobject.TestCaseId;
import com.platform.testing.domain.testcase.valueobject.TestType;
import com.platform.testing.domain.project.valueobject.ProjectId;

import java.util.List;
import java.util.Optional;

public interface TestCaseRepository {
    TestCase save(TestCase testCase);

    Optional<TestCase> findById(TestCaseId id);

    List<TestCase> findByProjectId(ProjectId projectId);

    List<TestCase> findByTag(String tag);

    List<TestCase> findByTestType(TestType testType);

    void deleteById(TestCaseId id);
}
