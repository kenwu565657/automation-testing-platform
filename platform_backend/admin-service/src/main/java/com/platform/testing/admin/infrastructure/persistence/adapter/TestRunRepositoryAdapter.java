package com.platform.testing.admin.infrastructure.persistence.adapter;

import com.platform.testing.domain.constant.RunStatus;
import com.platform.testing.domain.execution.TestRun;
import com.platform.testing.domain.execution.TestRunId;
import com.platform.testing.domain.execution.TestRunRepository;
import com.platform.testing.domain.testsuite.TestSuiteId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TestRunRepositoryAdapter implements TestRunRepository {

    @Override
    public TestRun save(TestRun testRun) {
        return null;
    }

    @Override
    public Optional<TestRun> findById(TestRunId id) {
        return Optional.empty();
    }

    @Override
    public List<TestRun> findByTestSuiteId(TestSuiteId testSuiteId) {
        return List.of();
    }

    @Override
    public List<TestRun> findByStatus(RunStatus status) {
        return List.of();
    }

    @Override
    public void deleteById(TestRunId id) {

    }
}

