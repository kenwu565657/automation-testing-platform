package com.valdifly.domain.organization.repository;

import com.valdifly.domain.organization.Organization;
import com.valdifly.domain.organization.valueobject.OrganizationId;
import java.util.List;
import java.util.Optional;

public interface OrganizationRepository {
    Organization save(Organization organization);

    Optional<Organization> findById(OrganizationId id);

    List<Organization> findAll();

    void deleteById(OrganizationId id);
}
