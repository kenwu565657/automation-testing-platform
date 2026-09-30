package com.valdifly.domain.testsuite.repository;

import com.valdifly.domain.testsuite.TestSuite;
import com.valdifly.domain.testsuite.valueobject.TestSuiteId;
import com.valdifly.domain.project.valueobject.ProjectId;
import java.util.List;
import java.util.Optional;

public interface TestSuiteRepository {
    TestSuite save(TestSuite testSuite);
    Optional<TestSuite> findById(TestSuiteId id);
    List<TestSuite> findByProjectId(ProjectId projectId);
    void deleteById(TestSuiteId id);
}
