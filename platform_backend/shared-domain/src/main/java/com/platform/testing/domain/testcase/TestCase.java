package com.platform.testing.domain.testcase;

import com.platform.testing.domain.testcase.valueobject.AuthoringStyle;
import com.platform.testing.domain.common.Priority;
import com.platform.testing.domain.testcase.valueobject.TestCaseId;
import com.platform.testing.domain.testcase.valueobject.TestType;
import com.platform.testing.domain.common.AggregateRoot;
import com.platform.testing.domain.project.valueobject.ProjectId;
import com.platform.testing.domain.user.valueobject.UserId;
import com.platform.testing.utils.TimeUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class TestCase implements AggregateRoot<TestCaseId> {

    private final TestCaseId id;
    private final ProjectId projectId;
    private String name;
    private String description;
    private final TestType testType;
    private final AuthoringStyle authoringStyle;
    private Priority priority;
    private final Set<String> tags;
    private boolean active;
    private int timeoutSeconds;
    private int retryCount;
    private int latestDefinitionVersion;
    private Instant createdAt;
    private Instant updatedAt;
    private final UserId createdBy;

    public static TestCase create(
            ProjectId projectId,
            String name,
            TestType testType,
            AuthoringStyle authoringStyle,
            Priority priority,
            UserId createdBy
    ) {
        return new TestCase(
                TestCaseId.generate(),
                projectId,
                name,
                testType,
                authoringStyle,
                priority,
                Objects.requireNonNull(createdBy, "createdBy is required")
        );
    }

    public static TestCase reconstitute(
            TestCaseId id,
            ProjectId projectId,
            String name,
            String description,
            TestType testType,
            AuthoringStyle authoringStyle,
            Priority priority,
            Set<String> tags,
            boolean active,
            int timeoutSeconds,
            int retryCount,
            int latestDefinitionVersion,
            Instant createdAt,
            Instant updatedAt,
            UserId createdBy
    ) {
        TestCase testCase = new TestCase(id, projectId, name, testType, authoringStyle, priority, createdBy);
        testCase.description = description;
        if (tags != null) {
            testCase.tags.addAll(tags);
        }
        testCase.active = active;
        testCase.timeoutSeconds = timeoutSeconds;
        testCase.retryCount = retryCount;
        testCase.latestDefinitionVersion = latestDefinitionVersion;
        testCase.createdAt = createdAt;
        testCase.updatedAt = updatedAt;
        return testCase;
    }

    private TestCase(
            TestCaseId id,
            ProjectId projectId,
            String name,
            TestType testType,
            AuthoringStyle authoringStyle,
            Priority priority,
            UserId createdBy
    ) {
        this.id = Objects.requireNonNull(id);
        this.projectId = Objects.requireNonNull(projectId, "projectId is required");
        this.name = requireName(name);
        this.testType = Objects.requireNonNull(testType, "testType is required");
        this.authoringStyle = Objects.requireNonNull(authoringStyle, "authoringStyle is required");
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy is required");
        this.tags = new LinkedHashSet<>();
        this.active = true;
        this.timeoutSeconds = 60;
        this.retryCount = 0;
        this.latestDefinitionVersion = 0;
        this.createdAt = TimeUtils.now();
        this.updatedAt = this.createdAt;
    }

    public void rename(String newName) {
        this.name = requireName(newName);
        touch();
    }

    public void updateDescription(String description) {
        this.description = description;
        touch();
    }

    public void changePriority(Priority priority) {
        this.priority = priority != null ? priority : Priority.MEDIUM;
        touch();
    }

    public void setPolicy(int timeoutSeconds, int retryCount) {
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeoutSeconds must be > 0");
        }
        if (retryCount < 0) {
            throw new IllegalArgumentException("retryCount cannot be negative");
        }
        this.timeoutSeconds = timeoutSeconds;
        this.retryCount = retryCount;
        touch();
    }

    public void addTag(String tag) {
        this.tags.add(normalizeTag(tag));
        touch();
    }

    public void removeTag(String tag) {
        this.tags.remove(normalizeTag(tag));
        touch();
    }

    public void deactivate() {
        this.active = false;
        touch();
    }

    public void activate() {
        this.active = true;
        touch();
    }

    public int nextDefinitionVersion() {
        return latestDefinitionVersion + 1;
    }

    public void recordPublishedDefinition(int version) {
        if (version != latestDefinitionVersion + 1) {
            throw new IllegalArgumentException(
                    "expected definition version " + (latestDefinitionVersion + 1) + " but was " + version);
        }
        this.latestDefinitionVersion = version;
        touch();
    }

    public boolean hasPublishedDefinition() {
        return latestDefinitionVersion > 0;
    }

    private void touch() {
        this.updatedAt = TimeUtils.now();
    }

    private static String requireName(String name) {
        Objects.requireNonNull(name, "name is required");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        return name;
    }

    private static String normalizeTag(String tag) {
        Objects.requireNonNull(tag, "tag is required");
        return tag.startsWith("@") ? tag : "@" + tag;
    }

    @Override
    public TestCaseId getId() {
        return id;
    }

    public ProjectId getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TestType getTestType() {
        return testType;
    }

    public AuthoringStyle getAuthoringStyle() {
        return authoringStyle;
    }

    public Priority getPriority() {
        return priority;
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public boolean isActive() {
        return active;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getLatestDefinitionVersion() {
        return latestDefinitionVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public UserId getCreatedBy() {
        return createdBy;
    }
}
