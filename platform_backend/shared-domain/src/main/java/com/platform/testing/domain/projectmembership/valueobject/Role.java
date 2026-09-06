package com.platform.testing.domain.projectmembership.valueobject;

import com.platform.testing.domain.project.valueobject.ProjectId;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum Role {
    VIEWER,
    RUNNER,
    EDITOR,
    OWNER;

    private static final Set<Action> VIEW = EnumSet.of(
            Action.PROJECT_GET,
            Action.CATALOG_READ,
            Action.ENVIRONMENT_READ,
            Action.TARGET_READ,
            Action.SCHEDULE_READ,
            Action.RUN_READ,
            Action.REPORT_READ
    );

    private static final Set<Action> RUN = union(VIEW, EnumSet.of(
            Action.RUN_TRIGGER,
            Action.RUN_CANCEL
    ));

    private static final Set<Action> EDIT = union(RUN, EnumSet.of(
            Action.CATALOG_WRITE,
            Action.ENVIRONMENT_WRITE,
            Action.TARGET_WRITE,
            Action.SCHEDULE_WRITE
    ));

    private static final Set<Action> OWN = union(EDIT, EnumSet.of(
            Action.PROJECT_UPDATE,
            Action.PROJECT_DELETE,
            Action.PROJECT_MANAGE_MEMBERS
    ));

    public Set<Action> actions() {
        return switch (this) {
            case VIEWER -> VIEW;
            case RUNNER -> RUN;
            case EDITOR -> EDIT;
            case OWNER -> OWN;
        };
    }

    public List<PolicyStatement> policy(ProjectId projectId) {
        return List.of(PolicyStatement.allow(actions(), ResourceArn.project(projectId)));
    }

    private static Set<Action> union(Set<Action> base, Set<Action> extra) {
        EnumSet<Action> combined = EnumSet.copyOf(base);
        combined.addAll(extra);
        return Set.copyOf(combined);
    }
}
