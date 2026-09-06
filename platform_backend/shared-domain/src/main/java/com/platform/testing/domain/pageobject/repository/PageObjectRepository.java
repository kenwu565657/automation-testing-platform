package com.platform.testing.domain.pageobject.repository;

import com.platform.testing.domain.pageobject.PageObject;
import com.platform.testing.domain.pageobject.valueobject.PageObjectId;
import com.platform.testing.domain.project.valueobject.ProjectId;

import java.util.List;
import java.util.Optional;

public interface PageObjectRepository {
    PageObject save(PageObject pageObject);
    Optional<PageObject> findById(PageObjectId id);
    List<PageObject> findByProjectId(ProjectId projectId);
    void deleteById(PageObjectId id);
}
