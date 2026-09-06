package com.platform.testing.domain.pageobject.entity;

import com.platform.testing.domain.common.DomainEntity;
import com.platform.testing.domain.device.valueobject.PlatformType;
import com.platform.testing.domain.pageobject.valueobject.ElementLocator;
import com.platform.testing.domain.pageobject.valueobject.PageElementId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PageElement implements DomainEntity<PageElementId> {

    private final PageElementId id;
    private final String name;
    private final String description;
    private final List<ElementLocator> locators;

    public static PageElement create(String name, String description) {
        return new PageElement(PageElementId.generate(), name, description);
    }

    public static PageElement reconstitute(PageElementId id, String name, String description,
                                           List<ElementLocator> locators) {
        PageElement pe = new PageElement(id, name, description);
        pe.locators.addAll(locators);
        return pe;
    }

    private PageElement(PageElementId id, String name, String description) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.locators = new ArrayList<>();
    }

    public void addLocator(ElementLocator locator) {
        boolean exists = locators.stream()
                .anyMatch(l -> l.platformType() == locator.platformType());
        if (exists) {
            throw new IllegalArgumentException(
                    "Locator already exists for platform: " + locator.platformType());
        }
        locators.add(locator);
    }

    public void replaceLocator(ElementLocator locator) {
        locators.removeIf(l -> l.platformType() == locator.platformType());
        locators.add(locator);
    }

    public Optional<ElementLocator> locatorFor(PlatformType platformType) {
        return locators.stream()
                .filter(l -> l.platformType() == platformType)
                .findFirst();
    }

    public PageElementId getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<ElementLocator> getLocators() { return Collections.unmodifiableList(locators); }
}
