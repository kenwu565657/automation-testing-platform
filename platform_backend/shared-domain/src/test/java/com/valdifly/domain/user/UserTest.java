package com.valdifly.domain.user;

import com.valdifly.domain.projectmembership.valueobject.ResourceArn;
import com.valdifly.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void createAssignsUserArn() {
        User user = User.create("qa@shop.test", "QA");
        assertEquals("qa@shop.test", user.getEmail());
        assertEquals("QA", user.getDisplayName());
        assertTrue(user.isActive());
        assertEquals(ResourceArn.user(user.getId()), user.resourceArn());
    }

    @Test
    void rejectsInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () -> User.create("not-an-email", "QA"));
        assertThrows(IllegalArgumentException.class, () -> User.create("  ", "QA"));
        assertThrows(NullPointerException.class, () -> User.create("qa@shop.test", null));
    }

    @Test
    void renameChangeEmailAndDeactivate() {
        User user = User.create("qa@shop.test", "QA");
        user.rename("Lead QA");
        user.changeEmail("lead@shop.test");
        user.deactivate();

        assertEquals("Lead QA", user.getDisplayName());
        assertEquals("lead@shop.test", user.getEmail());
        assertFalse(user.isActive());
        user.activate();
        assertTrue(user.isActive());
    }

    @Test
    void reconstitute() {
        Instant created = Instant.parse("2026-01-01T00:00:00Z");
        User restored = User.reconstitute(
                UserId.of("u1"), "qa@shop.test", "QA", "hash", false, created, created
        );
        assertEquals("u1", restored.getId().value());
        assertEquals("hash", restored.getPasswordHash());
        assertFalse(restored.isActive());
    }

    @Test
    void storesAndReplacesPasswordHash() {
        User user = User.create("qa@shop.test", "QA", "first-hash");
        assertEquals("first-hash", user.getPasswordHash());
        user.changePasswordHash("second-hash");
        assertEquals("second-hash", user.getPasswordHash());
        assertThrows(IllegalArgumentException.class, () -> user.changePasswordHash(" "));
    }
}
