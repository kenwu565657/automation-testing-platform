package com.valdifly.domain.pageobject;

import com.valdifly.domain.common.AggregateRoot;
import com.valdifly.domain.pageobject.entity.PageElement;
import com.valdifly.domain.pageobject.valueobject.ElementLocator;
import com.valdifly.domain.pageobject.valueobject.PageElementId;
import com.valdifly.domain.pageobject.valueobject.PageObjectId;
import com.valdifly.domain.project.valueobject.ProjectId;
import com.valdifly.utils.TimeUtils;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PageObject implements AggregateRoot<PageObjectId> {

    private final PageObjectId id;
    private final String name;
    private final String description;
    private final String pageUrl;
    private final ProjectId projectId;
    private final List<PageElement> elements;
    private Instant createdAt;
    private Instant updatedAt;

    public static PageObject create(String name, String description, String pageUrl, ProjectId projectId) {
        return new PageObject(PageObjectId.generate(), name, description, pageUrl, projectId);
    }

    public static PageObject reconstitute(PageObjectId id, String name, String description,
                                          String pageUrl, ProjectId projectId, List<PageElement> elements,
                                          Instant createdAt, Instant updatedAt) {
        PageObject po = new PageObject(id, name, description, pageUrl, projectId);
        po.elements.addAll(elements);
        po.createdAt = createdAt;
        po.updatedAt = updatedAt;
        return po;
    }

    private PageObject(PageObjectId id, String name, String description, String pageUrl, ProjectId projectId) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.pageUrl = pageUrl;
        this.projectId = Objects.requireNonNull(projectId);
        this.elements = new ArrayList<>();
        this.createdAt = TimeUtils.now();
        this.updatedAt = this.createdAt;
    }

    public void addElement(PageElement element) {
        boolean nameExists = elements.stream().anyMatch(e -> e.getName().equals(element.getName()));
        if (nameExists) {
            throw new IllegalArgumentException("Element with name already exists: " + element.getName());
        }
        elements.add(element);
        touch();
    }

    public void removeElement(PageElementId elementId) {
        elements.removeIf(e -> e.getId().equals(elementId));
        touch();
    }

    public void replaceElements(List<PageElement> newElements) {
        Objects.requireNonNull(newElements, "elements is required");
        elements.clear();
        for (PageElement element : newElements) {
            Objects.requireNonNull(element, "element is required");
            boolean nameExists = elements.stream().anyMatch(existing -> existing.getName().equals(element.getName()));
            if (nameExists) {
                throw new IllegalArgumentException("Element with name already exists: " + element.getName());
            }
            elements.add(element);
        }
        touch();
    }

    public Optional<PageElement> findElementByName(String name) {
        return elements.stream().filter(e -> e.getName().equals(name)).findFirst();
    }

    public Optional<PageElement> findElementById(PageElementId elementId) {
        return elements.stream().filter(element -> element.getId().equals(elementId)).findFirst();
    }

    public void healLocator(PageElementId elementId, ElementLocator healed) {
        Objects.requireNonNull(elementId, "elementId is required");
        Objects.requireNonNull(healed, "healed locator is required");
        PageElement element = findElementById(elementId)
                .orElseThrow(() -> new IllegalArgumentException("Element not found: " + elementId.value()));
        Optional<ElementLocator> current = element.locatorFor(healed.platformType());
        ElementLocator next = current
                .map(existing -> new ElementLocator(
                        healed.platformType(),
                        healed.locatorStrategy(),
                        healed.locatorValue(),
                        existing.locatorStrategy(),
                        existing.locatorValue()
                ))
                .orElse(healed);
        element.replaceLocator(next);
        touch();
    }

    private void touch() {
        this.updatedAt = TimeUtils.now();
    }

    public PageObjectId getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPageUrl() { return pageUrl; }
    public ProjectId getProjectId() { return projectId; }
    public List<PageElement> getElements() { return Collections.unmodifiableList(elements); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public Map<String, Map<String, String>> toLocatorMap() {
        Map<String, Map<String, String>> map = new HashMap<>();
        for (PageElement element : elements) {
            if (!element.getLocators().isEmpty()) {
                ElementLocator locator = element.getLocators().getFirst();
                Map<String, String> locatorDetails = new HashMap<>();
                locatorDetails.put("strategy", locator.locatorStrategy().name());
                locatorDetails.put("value", locator.locatorValue());
                locatorDetails.put("elementId", element.getId().value());
                if (locator.fallbackStrategy() != null && locator.fallbackValue() != null) {
                    locatorDetails.put("fallbackStrategy", locator.fallbackStrategy().name());
                    locatorDetails.put("fallbackValue", locator.fallbackValue());
                }
                map.put(element.getName(), locatorDetails);
            }
        }
        return map;
    }
}
