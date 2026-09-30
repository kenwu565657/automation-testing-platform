package com.valdifly.domain.common;

/**
 * Published names of domain events (what happened).
 * Services map these names to broker topics.
 */
public final class EventType {

    private EventType() {}

    // ── Admin → Coordinator ──
    public static final String TEST_EXECUTION_REQUEST = "test.execution.request";
    public static final String TEST_RUN_CANCELLED     = "test.run.cancelled";

    // ── Coordinator → one engine, and that engine → coordinator ──
    public static final String ENGINE_ASSIGNMENT      = "engine.assignment";
    public static final String ENGINE_STATUS          = "engine.status";

    // ── Engine → Report ──
    public static final String TEST_RUN_STARTED       = "test.run.started";
    public static final String TEST_RUN_COMPLETED     = "test.run.completed";
    public static final String TEST_CASE_STARTED      = "test.case.started";
    public static final String TEST_CASE_COMPLETED    = "test.case.completed";
    public static final String TEST_STEP_COMPLETED    = "test.step.completed";

    // ── Admin → Anyone (lifecycle) ──
    public static final String TEST_CASE_CREATED      = "test.case.created";
    public static final String TEST_CASE_UPDATED      = "test.case.updated";
}
