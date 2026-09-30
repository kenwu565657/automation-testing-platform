package com.valdifly.domain.organizationmembership.repository;

import com.valdifly.domain.organization.valueobject.OrganizationId;
import com.valdifly.domain.organizationmembership.OrganizationMembership;
import com.valdifly.domain.organizationmembership.valueobject.OrganizationMembershipId;
import com.valdifly.domain.user.valueobject.UserId;
import java.util.List;
import java.util.Optional;

public interface OrganizationMembershipRepository {
    OrganizationMembership save(OrganizationMembership membership);

    Optional<OrganizationMembership> findById(OrganizationMembershipId id);

    Optional<OrganizationMembership> findByUserIdAndOrganizationId(UserId userId, OrganizationId organizationId);

    List<OrganizationMembership> findByUserId(UserId userId);

    List<OrganizationMembership> findByOrganizationId(OrganizationId organizationId);

    void deleteById(OrganizationMembershipId id);
}
