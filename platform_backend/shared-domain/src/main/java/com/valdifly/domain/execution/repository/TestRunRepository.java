package com.valdifly.domain.execution.repository;

import com.valdifly.domain.testsuite.valueobject.TestSuiteId;
import com.valdifly.domain.execution.TestRun;
import com.valdifly.domain.execution.valueobject.RunStatus;
import com.valdifly.domain.execution.valueobject.TestRunId;

import java.util.List;
import java.util.Optional;

public interface TestRunRepository {
    TestRun save(TestRun testRun);
    Optional<TestRun> findById(TestRunId id);
    List<TestRun> findByTestSuiteId(TestSuiteId testSuiteId);
    List<TestRun> findByStatus(RunStatus status);
    void deleteById(TestRunId id);
}
