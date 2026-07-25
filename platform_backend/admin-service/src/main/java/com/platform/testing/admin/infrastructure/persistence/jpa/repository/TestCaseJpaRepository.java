package com.platform.testing.admin.infrastructure.persistence.jpa.repository;

import com.platform.testing.admin.infrastructure.persistence.jpa.entity.TestCaseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface TestCaseJpaRepository extends JpaRepository<TestCaseJpaEntity, String> {

    List<TestCaseJpaEntity> findByTestSuiteIdAndActiveTrue(String testSuiteId);

    List<TestCaseJpaEntity> findByTestType(String testType);

    @Query("SELECT tc FROM TestCaseJpaEntity tc LEFT JOIN FETCH tc.steps LEFT JOIN FETCH tc.tags WHERE tc.id = :id")
    Optional<TestCaseJpaEntity> findByIdWithDetails(String id);

    @Query("SELECT DISTINCT tc FROM TestCaseJpaEntity tc JOIN tc.tags t WHERE t = :tag")
    List<TestCaseJpaEntity> findByTag(String tag);
}
