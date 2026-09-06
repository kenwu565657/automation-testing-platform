package com.platform.testing.domain.testsuite.repository;

import com.platform.testing.domain.testsuite.TestSuite;
import com.platform.testing.domain.testsuite.valueobject.TestSuiteId;
import com.platform.testing.domain.project.valueobject.ProjectId;

import java.util.List;
import java.util.Optional;

public interface TestSuiteRepository {
    TestSuite save(TestSuite testSuite);
    Optional<TestSuite> findById(TestSuiteId id);
    List<TestSuite> findByProjectId(ProjectId projectId);
    void deleteById(TestSuiteId id);
}
