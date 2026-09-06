package com.platform.testing.domain.user;

import com.platform.testing.domain.projectmembership.valueobject.ResourceArn;
import com.platform.testing.domain.user.valueobject.UserId;
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
    }

    @Test
    void reconstitute() {
        Instant created = Instant.parse("2026-01-01T00:00:00Z");
        User restored = User.reconstitute(
                UserId.of("u1"), "qa@shop.test", "QA", false, created, created
        );
        assertEquals("u1", restored.getId().value());
        assertFalse(restored.isActive());
    }
}
