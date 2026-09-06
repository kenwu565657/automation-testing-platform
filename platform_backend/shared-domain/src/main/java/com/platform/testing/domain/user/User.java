package com.platform.testing.domain.user;

import com.platform.testing.domain.common.AggregateRoot;
import com.platform.testing.domain.projectmembership.valueobject.ResourceArn;
import com.platform.testing.domain.user.valueobject.UserId;
import com.platform.testing.utils.TimeUtils;

import java.time.Instant;
import java.util.Objects;

public class User implements AggregateRoot<UserId> {

    private final UserId id;
    private String email;
    private String displayName;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static User create(String email, String displayName) {
        return new User(UserId.generate(), email, displayName);
    }

    public static User reconstitute(
            UserId id,
            String email,
            String displayName,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        User user = new User(id, email, displayName);
        user.active = active;
        user.createdAt = createdAt;
        user.updatedAt = updatedAt;
        return user;
    }

    private User(UserId id, String email, String displayName) {
        this.id = Objects.requireNonNull(id);
        this.email = requireEmail(email);
        this.displayName = requireDisplayName(displayName);
        this.active = true;
        this.createdAt = TimeUtils.now();
        this.updatedAt = this.createdAt;
    }

    public void rename(String displayName) {
        this.displayName = requireDisplayName(displayName);
        touch();
    }

    public void changeEmail(String email) {
        this.email = requireEmail(email);
        touch();
    }

    public void deactivate() {
        this.active = false;
        touch();
    }

    public ResourceArn resourceArn() {
        return ResourceArn.user(id);
    }

    private void touch() {
        this.updatedAt = TimeUtils.now();
    }

    private static String requireEmail(String email) {
        Objects.requireNonNull(email, "email is required");
        if (email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("email is invalid");
        }
        return email;
    }

    private static String requireDisplayName(String displayName) {
        Objects.requireNonNull(displayName, "displayName is required");
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("displayName cannot be blank");
        }
        return displayName;
    }

    @Override
    public UserId getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
