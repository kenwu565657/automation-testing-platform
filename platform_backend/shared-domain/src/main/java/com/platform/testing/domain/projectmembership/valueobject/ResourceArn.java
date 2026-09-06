package com.platform.testing.domain.projectmembership.valueobject;

import com.platform.testing.domain.common.ValueObject;
import com.platform.testing.domain.user.valueobject.UserId;
import com.platform.testing.domain.project.valueobject.ProjectId;

import java.util.Objects;
import java.util.Optional;

public record ResourceArn(String value) implements ValueObject<String> {

    public static final String PREFIX = "arn:testops:";

    public ResourceArn {
        Objects.requireNonNull(value, "resource ARN is required");
        if (!value.startsWith(PREFIX)) {
            throw new IllegalArgumentException("resource ARN must start with " + PREFIX);
        }
        if (value.length() == PREFIX.length()) {
            throw new IllegalArgumentException("resource ARN is incomplete");
        }
    }

    public static ResourceArn user(UserId userId) {
        return new ResourceArn(PREFIX + "user:" + userId.value());
    }

    public static ResourceArn project(ProjectId projectId) {
        return new ResourceArn(PREFIX + "project:" + projectId.value());
    }

    public static ResourceArn of(ProjectId projectId, String resourceType, String resourceId) {
        Objects.requireNonNull(resourceType, "resourceType is required");
        Objects.requireNonNull(resourceId, "resourceId is required");
        if (resourceType.isBlank() || resourceId.isBlank()) {
            throw new IllegalArgumentException("resourceType and resourceId cannot be blank");
        }
        return new ResourceArn(PREFIX + "project:" + projectId.value() + ":" + resourceType + ":" + resourceId);
    }

    public boolean covers(ResourceArn requested) {
        Objects.requireNonNull(requested);
        return requested.value.equals(value) || requested.value.startsWith(value + ":");
    }

    public Optional<ProjectId> projectId() {
        String rest = value.substring(PREFIX.length());
        if (!rest.startsWith("project:")) {
            return Optional.empty();
        }
        String remainder = rest.substring("project:".length());
        int colon = remainder.indexOf(':');
        String id = colon < 0 ? remainder : remainder.substring(0, colon);
        return Optional.of(ProjectId.of(id));
    }
}
