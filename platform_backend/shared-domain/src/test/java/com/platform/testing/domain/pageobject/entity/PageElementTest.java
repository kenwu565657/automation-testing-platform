package com.platform.testing.domain.pageobject.entity;

import com.platform.testing.domain.device.valueobject.PlatformType;
import com.platform.testing.domain.pageobject.valueobject.ElementLocator;
import com.platform.testing.domain.pageobject.valueobject.LocatorStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageElementTest {

    @Test
    void oneLocatorPerPlatform() {
        PageElement element = PageElement.create("email", "field");
        ElementLocator web = new ElementLocator(PlatformType.WEB_DESKTOP, LocatorStrategy.ID, "email", null, null);
        element.addLocator(web);
        assertThrows(IllegalArgumentException.class, () -> element.addLocator(
                new ElementLocator(PlatformType.WEB_DESKTOP, LocatorStrategy.NAME, "email", null, null)
        ));

        element.replaceLocator(new ElementLocator(PlatformType.WEB_DESKTOP, LocatorStrategy.NAME, "user", null, null));
        assertTrue(element.locatorFor(PlatformType.WEB_DESKTOP).isPresent());
        assertEquals("user", element.locatorFor(PlatformType.WEB_DESKTOP).orElseThrow().locatorValue());
    }
}
