package com.valdifly.domain.pageobject;

import com.valdifly.domain.device.valueobject.PlatformType;
import com.valdifly.domain.pageobject.entity.PageElement;
import com.valdifly.domain.pageobject.valueobject.ElementLocator;
import com.valdifly.domain.pageobject.valueobject.LocatorStrategy;
import com.valdifly.domain.project.valueobject.ProjectId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageObjectTest {

    @Test
    void addElementRejectsDuplicateName() {
        PageObject page = PageObject.create("Login", "form", "/login", ProjectId.generate());
        page.addElement(PageElement.create("submit", "button"));
        assertThrows(IllegalArgumentException.class, () -> page.addElement(PageElement.create("submit", "other")));
    }

    @Test
    void findAndRemoveElement() {
        PageObject page = PageObject.create("Login", null, "/login", ProjectId.generate());
        PageElement submit = PageElement.create("submit", "button");
        page.addElement(submit);

        assertTrue(page.findElementByName("submit").isPresent());
        page.removeElement(submit.getId());
        assertTrue(page.findElementByName("submit").isEmpty());
    }

    @Test
    void locatorMapUsesFirstLocator() {
        PageObject page = PageObject.create("Login", null, "/login", ProjectId.generate());
        PageElement submit = PageElement.create("submit", "button");
        submit.addLocator(new ElementLocator(PlatformType.WEB_DESKTOP, LocatorStrategy.CSS_SELECTOR, "#go", null, null));
        page.addElement(submit);

        assertEquals("CSS_SELECTOR", page.toLocatorMap().get("submit").get("strategy"));
        assertEquals("#go", page.toLocatorMap().get("submit").get("value"));
        assertEquals(submit.getId().value(), page.toLocatorMap().get("submit").get("elementId"));
    }

    @Test
    void healPromotesNewLocatorAndKeepsPreviousAsFallback() {
        PageObject page = PageObject.create("Login", null, "/login", ProjectId.generate());
        PageElement submit = PageElement.create("submit", "button");
        submit.addLocator(new ElementLocator(PlatformType.WEB_DESKTOP, LocatorStrategy.ID, "old", null, null));
        page.addElement(submit);

        page.healLocator(submit.getId(), new ElementLocator(
                PlatformType.WEB_DESKTOP, LocatorStrategy.CSS_SELECTOR, "#go", null, null
        ));

        ElementLocator locator = page.findElementById(submit.getId()).orElseThrow().getLocators().getFirst();
        assertEquals(LocatorStrategy.CSS_SELECTOR, locator.locatorStrategy());
        assertEquals("#go", locator.locatorValue());
        assertEquals(LocatorStrategy.ID, locator.fallbackStrategy());
        assertEquals("old", locator.fallbackValue());
        assertEquals("ID", page.toLocatorMap().get("submit").get("fallbackStrategy"));
        assertEquals("old", page.toLocatorMap().get("submit").get("fallbackValue"));
    }

    @Test
    void replaceElementsRewritesMembership() {
        PageObject page = PageObject.create("Login", null, "/login", ProjectId.generate());
        page.addElement(PageElement.create("old", "gone"));

        PageElement submit = PageElement.create("submit", "button");
        page.replaceElements(List.of(submit));

        assertTrue(page.findElementByName("old").isEmpty());
        assertTrue(page.findElementByName("submit").isPresent());
        assertThrows(IllegalArgumentException.class, () ->
                page.replaceElements(List.of(PageElement.create("dup", null), PageElement.create("dup", null))));
    }
}
