package com.valdifly.domain.execution.valueobject;

public enum RunStatus {
    QUEUED,
    RUNNING,
    PASSED,
    FAILED,
    ERROR,
    SKIPPED,
    CANCELLED
}
