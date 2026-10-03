package com.valdifly.domain.user;

import com.valdifly.domain.common.AggregateRoot;
import com.valdifly.domain.common.ResourceArn;
import com.valdifly.domain.user.valueobject.UserId;
import com.valdifly.utils.TimeUtils;

import java.time.Instant;
import java.util.Objects;

public class User implements AggregateRoot<UserId> {

    private final UserId id;
    private String email;
    private String displayName;
    private String passwordHash;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static User create(String email, String displayName) {
        return create(email, displayName, null);
    }

    public static User create(String email, String displayName, String passwordHash) {
        User user = new User(UserId.generate(), email, displayName);
        user.passwordHash = normalizeHash(passwordHash);
        return user;
    }

    public static User reconstitute(
            UserId id,
            String email,
            String displayName,
            String passwordHash,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        User user = new User(id, email, displayName);
        user.passwordHash = normalizeHash(passwordHash);
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

    public void activate() {
        this.active = true;
        touch();
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = requireHash(passwordHash);
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

    private static String normalizeHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            return null;
        }
        return passwordHash;
    }

    private static String requireHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash is required");
        }
        return passwordHash;
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

    public String getPasswordHash() {
        return passwordHash;
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
