package com.valdifly.domain.pageobject.repository;

import com.valdifly.domain.pageobject.PageObject;
import com.valdifly.domain.pageobject.valueobject.PageObjectId;
import com.valdifly.domain.project.valueobject.ProjectId;

import java.util.List;
import java.util.Optional;

public interface PageObjectRepository {
    PageObject save(PageObject pageObject);
    Optional<PageObject> findById(PageObjectId id);
    List<PageObject> findByProjectId(ProjectId projectId);
    void deleteById(PageObjectId id);
}
