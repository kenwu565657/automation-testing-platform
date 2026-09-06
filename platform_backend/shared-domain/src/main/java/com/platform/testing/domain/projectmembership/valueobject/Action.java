package com.platform.testing.domain.projectmembership.valueobject;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Action {
    PROJECT_GET("project:Get"),
    PROJECT_UPDATE("project:Update"),
    PROJECT_DELETE("project:Delete"),
    PROJECT_MANAGE_MEMBERS("project:ManageMembers"),

    CATALOG_READ("catalog:Read"),
    CATALOG_WRITE("catalog:Write"),

    ENVIRONMENT_READ("environment:Read"),
    ENVIRONMENT_WRITE("environment:Write"),

    TARGET_READ("target:Read"),
    TARGET_WRITE("target:Write"),

    SCHEDULE_READ("schedule:Read"),
    SCHEDULE_WRITE("schedule:Write"),

    RUN_READ("run:Read"),
    RUN_TRIGGER("run:Trigger"),
    RUN_CANCEL("run:Cancel"),

    REPORT_READ("report:Read");

    private static final Map<String, Action> BY_IAM_NAME = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(Action::iamName, Function.identity()));

    private final String iamName;

    Action(String iamName) {
        this.iamName = iamName;
    }

    public String iamName() {
        return iamName;
    }

    public static Action fromIamName(String iamName) {
        Action action = BY_IAM_NAME.get(iamName);
        if (action == null) {
            throw new IllegalArgumentException("Unknown action: " + iamName);
        }
        return action;
    }
}
