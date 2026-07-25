package com.platform.testing.admin.infrastructure.persistence.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Entity
@Table(name = "TEST_CASE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String description;
    private String featureTitle;
    private String scenarioTitle;
    private String testType;
    private String priority;
    private String testSuiteId;
    private boolean active;
    private int timeoutSeconds;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    @OneToMany(mappedBy = "testCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<TestStepJpaEntity> steps = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "test_case_tags", joinColumns = @JoinColumn(name = "test_case_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @OneToMany(mappedBy = "testCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TestParameterJpaEntity> parameters = new ArrayList<>();

    public void addStep(TestStepJpaEntity step) {
        step.setTestCase(this);
        this.steps.add(step);
    }

    public void clearSteps() {
        this.steps.forEach(s -> s.setTestCase(null));
        this.steps.clear();
    }
}
