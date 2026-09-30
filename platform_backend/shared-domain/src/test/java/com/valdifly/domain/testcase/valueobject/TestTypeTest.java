package com.valdifly.domain.testcase.valueobject;

import com.valdifly.domain.testdefinition.valueobject.ActionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestTypeTest {

    @Test
    void apiAllowsHttpAndRejectsUiActions() {
        assertTrue(TestType.API.allowedActions().contains(ActionType.HTTP_REQUEST));
        assertFalse(TestType.API.allowedActions().contains(ActionType.CLICK));
        assertFalse(TestType.API.allowedActions().contains(ActionType.CALL_TEST_CASE));
    }

    @Test
    void loadTestAllowsCallOfApiCasesOnlyAsAnAction() {
        assertTrue(TestType.LOAD_TEST.allowedActions().contains(ActionType.CALL_TEST_CASE));
        assertTrue(TestType.LOAD_TEST.allowedActions().contains(ActionType.RAMP_UP));
        assertFalse(TestType.LOAD_TEST.allowedActions().contains(ActionType.HTTP_REQUEST));
    }

    @Test
    void webAndMobileKeepTheirOwnUiActions() {
        assertTrue(TestType.WEB_E2E.allowedActions().contains(ActionType.NAVIGATE_TO));
        assertTrue(TestType.WEB_E2E.allowedActions().contains(ActionType.CLICK));
        assertTrue(TestType.WEB_E2E.allowedActions().contains(ActionType.ASSERT_VISUAL));
        assertFalse(TestType.WEB_E2E.allowedActions().contains(ActionType.TAP));
        assertTrue(TestType.MOBILE_E2E.allowedActions().contains(ActionType.TAP));
        assertTrue(TestType.MOBILE_E2E.allowedActions().contains(ActionType.ASSERT_VISUAL));
        assertFalse(TestType.MOBILE_E2E.allowedActions().contains(ActionType.NAVIGATE_TO));
    }
}
