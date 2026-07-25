package com.platform.testing.admin.infrastructure.persistence.jpa.repository;

import com.platform.testing.admin.infrastructure.persistence.jpa.entity.TestSuiteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestSuiteJpaRepository extends JpaRepository<TestSuiteJpaEntity, String> {
}
