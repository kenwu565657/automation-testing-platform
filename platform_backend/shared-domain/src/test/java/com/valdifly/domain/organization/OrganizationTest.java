package com.valdifly.domain.organization;

import com.valdifly.domain.organization.valueobject.OrganizationId;
import com.valdifly.domain.common.ResourceArn;
import com.valdifly.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrganizationTest {

    @Test
    void createRenameAndPause() {
        Organization organization = Organization.create("Acme", "qa", UserId.of("ken"));
        assertTrue(organization.isActive());
        assertEquals(ResourceArn.organization(organization.getId()), organization.resourceArn());

        organization.rename("Acme QA");
        organization.changeDescription("labs");
        organization.deactivate();

        assertEquals("Acme QA", organization.getName());
        assertEquals("labs", organization.getDescription());
        assertFalse(organization.isActive());
        organization.activate();
        assertTrue(organization.isActive());
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
                Organization.create(" ", null, UserId.of("ken")));
    }

    @Test
    void reconstitutePreservesState() {
        OrganizationId id = OrganizationId.of("org-1");
        Instant created = Instant.parse("2026-03-01T08:00:00Z");
        Organization organization = Organization.reconstitute(
                id, "Acme", "d", false, created, created.plusSeconds(60), UserId.of("ken")
        );

        assertEquals(id, organization.getId());
        assertFalse(organization.isActive());
        assertEquals(created, organization.getCreatedAt());
    }
}
