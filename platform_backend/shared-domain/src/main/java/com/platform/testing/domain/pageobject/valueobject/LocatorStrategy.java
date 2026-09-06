package com.platform.testing.domain.pageobject.valueobject;

public enum LocatorStrategy {
    // ── Web (Selenium) ──
    ID, NAME, CSS_SELECTOR, XPATH, CLASS_NAME,
    TAG_NAME, LINK_TEXT, PARTIAL_LINK_TEXT,
    // ── Mobile (Appium) ──
    ACCESSIBILITY_ID, ANDROID_UIAUTOMATOR, ANDROID_VIEWTAG,
    ANDROID_DATA_MATCHER, IOS_PREDICATE, IOS_CLASS_CHAIN
}
