package com.platform.testing.domain.projectmembership.service;

import com.platform.testing.domain.projectmembership.ProjectMembership;
import com.platform.testing.domain.projectmembership.valueobject.Action;
import com.platform.testing.domain.projectmembership.valueobject.ResourceArn;

import java.util.Objects;

public final class AccessControl {

    private AccessControl() {}

    public static boolean allows(Iterable<ProjectMembership> memberships, Action action, ResourceArn resource) {
        Objects.requireNonNull(memberships);
        Objects.requireNonNull(action);
        Objects.requireNonNull(resource);
        boolean denied = false;
        boolean allowed = false;
        for (ProjectMembership membership : memberships) {
            if (membership.policy().stream().anyMatch(statement -> statement.denies(action, resource))) {
                denied = true;
            }
            if (membership.allows(action, resource)) {
                allowed = true;
            }
        }
        return allowed && !denied;
    }
}
