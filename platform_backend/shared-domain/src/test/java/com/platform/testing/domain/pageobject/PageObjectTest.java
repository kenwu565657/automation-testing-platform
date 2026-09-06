package com.platform.testing.domain.pageobject;

import com.platform.testing.domain.device.valueobject.PlatformType;
import com.platform.testing.domain.pageobject.entity.PageElement;
import com.platform.testing.domain.pageobject.valueobject.ElementLocator;
import com.platform.testing.domain.pageobject.valueobject.LocatorStrategy;
import com.platform.testing.domain.project.valueobject.ProjectId;
import org.junit.jupiter.api.Test;

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
    }
}
