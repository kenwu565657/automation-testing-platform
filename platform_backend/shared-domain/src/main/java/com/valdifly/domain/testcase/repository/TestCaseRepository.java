package com.valdifly.domain.testcase.repository;

import com.valdifly.domain.testcase.TestCase;
import com.valdifly.domain.testcase.valueobject.TestCaseId;
import com.valdifly.domain.testcase.valueobject.TestType;
import com.valdifly.domain.project.valueobject.ProjectId;

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
