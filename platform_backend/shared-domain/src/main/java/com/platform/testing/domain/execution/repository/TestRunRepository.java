package com.platform.testing.domain.execution.repository;

import com.platform.testing.domain.testsuite.valueobject.TestSuiteId;
import com.platform.testing.domain.execution.TestRun;
import com.platform.testing.domain.execution.valueobject.RunStatus;
import com.platform.testing.domain.execution.valueobject.TestRunId;

import java.util.List;
import java.util.Optional;

public interface TestRunRepository {
    TestRun save(TestRun testRun);
    Optional<TestRun> findById(TestRunId id);
    List<TestRun> findByTestSuiteId(TestSuiteId testSuiteId);
    List<TestRun> findByStatus(RunStatus status);
    void deleteById(TestRunId id);
}
