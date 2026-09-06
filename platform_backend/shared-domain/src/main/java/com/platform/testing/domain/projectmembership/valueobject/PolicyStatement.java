package com.platform.testing.domain.projectmembership.valueobject;

import java.util.Objects;
import java.util.Set;

public record PolicyStatement(
        Effect effect,
        Set<Action> actions,
        ResourceArn resource
) {
    public PolicyStatement {
        Objects.requireNonNull(effect, "effect is required");
        Objects.requireNonNull(actions, "actions are required");
        Objects.requireNonNull(resource, "resource is required");
        if (actions.isEmpty()) {
            throw new IllegalArgumentException("actions cannot be empty");
        }
        actions = Set.copyOf(actions);
    }

    public static PolicyStatement allow(Set<Action> actions, ResourceArn resource) {
        return new PolicyStatement(Effect.ALLOW, actions, resource);
    }

    public static PolicyStatement deny(Set<Action> actions, ResourceArn resource) {
        return new PolicyStatement(Effect.DENY, actions, resource);
    }

    public boolean permits(Action action, ResourceArn requested) {
        return effect == Effect.ALLOW
                && actions.contains(action)
                && resource.covers(requested);
    }

    public boolean denies(Action action, ResourceArn requested) {
        return effect == Effect.DENY
                && actions.contains(action)
                && resource.covers(requested);
    }
}
