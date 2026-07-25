package com.platform.testing.admin.infrastructure.persistence.adapter;

import com.platform.testing.domain.project.ProjectId;
import com.platform.testing.domain.testsuite.TestSuite;
import com.platform.testing.domain.testsuite.TestSuiteId;
import com.platform.testing.domain.testsuite.TestSuiteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TestSuiteRepositoryAdapter implements TestSuiteRepository {
    @Override
    public TestSuite save(TestSuite testSuite) {
        return null;
    }

    @Override
    public Optional<TestSuite> findById(TestSuiteId id) {
        return Optional.empty();
    }

    @Override
    public List<TestSuite> findByProjectId(ProjectId projectId) {
        return List.of();
    }

    @Override
    public void deleteById(TestSuiteId id) {

    }
}
