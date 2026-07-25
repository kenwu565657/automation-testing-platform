package com.platform.testing.admin.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;

@Entity
@Table(name = "test_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestStepJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id", nullable = false)
    private TestCaseJpaEntity testCase;

    private int orderIndex;
    private String name;

    @Column(name = "gherkin_keyword")
    private String gherkinKeyword;

    @Column(name = "gherkin_step_text")
    private String gherkinStepText;

    private String actionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> actionParameters;

    private String targetElementId;

    // assertion
    private String assertionType;
    private String assertionOperator;
    private String expectedValue;
    private String actualValueSource;

    // extraction
    @Column(name = "extraction_var_name")
    private String extractionVarName;
    @Column(name = "extraction_source")
    private String extractionSource;
    @Column(name = "extraction_expression")
    private String extractionExpression;

    @Column(name = "is_background")
    private boolean background;
    private boolean continueOnFailure;
    private int waitBeforeMs;
    private int waitAfterMs;
}
