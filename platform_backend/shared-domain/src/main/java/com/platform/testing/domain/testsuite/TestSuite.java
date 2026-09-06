package com.platform.testing.domain.testsuite;

import com.platform.testing.domain.testcase.valueobject.TestCaseId;
import com.platform.testing.domain.testsuite.valueobject.TestSuiteId;
import com.platform.testing.domain.common.AggregateRoot;
import com.platform.testing.domain.project.valueobject.ProjectId;
import com.platform.testing.utils.TimeUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TestSuite implements AggregateRoot<TestSuiteId> {

    private final TestSuiteId id;
    private String name;
    private String description;
    private ProjectId projectId;
    private final List<TestCaseId> testCaseIds;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static TestSuite create(String name, String description, ProjectId projectId) {
        return new TestSuite(TestSuiteId.generate(), name, description, projectId);
    }

    public static TestSuite reconstitute(TestSuiteId id, String name, String description,
                                         ProjectId projectId, List<TestCaseId> testCaseIds,
                                         boolean active, Instant createdAt, Instant updatedAt) {
        TestSuite ts = new TestSuite(id, name, description, projectId);
        ts.testCaseIds.addAll(testCaseIds);
        ts.active = active;
        ts.createdAt = createdAt;
        ts.updatedAt = updatedAt;
        return ts;
    }

    private TestSuite(TestSuiteId id, String name, String description, ProjectId projectId) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.projectId = Objects.requireNonNull(projectId);
        this.testCaseIds = new ArrayList<>();
        this.active = true;
        this.createdAt = TimeUtils.now();
        this.updatedAt = this.createdAt;
    }

    public void addTestCase(TestCaseId testCaseId) {
        if (testCaseIds.contains(testCaseId)) {
            throw new IllegalArgumentException("TestCase already in suite: " + testCaseId.value());
        }
        testCaseIds.add(testCaseId);
        touch();
    }

    public void removeTestCase(TestCaseId testCaseId) {
        testCaseIds.remove(testCaseId);
        touch();
    }

    public void deactivate() {
        this.active = false;
        touch();
    }

    private void touch() {
        this.updatedAt = TimeUtils.now();
    }

    public TestSuiteId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProjectId getProjectId() {
        return projectId;
    }

    public List<TestCaseId> getTestCaseIds() {
        return Collections.unmodifiableList(testCaseIds);
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
