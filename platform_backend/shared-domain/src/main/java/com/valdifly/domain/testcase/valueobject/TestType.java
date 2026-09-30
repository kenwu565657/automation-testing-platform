package com.valdifly.domain.testcase.valueobject;

import com.valdifly.domain.testdefinition.valueobject.ActionType;
import java.util.Set;

public enum TestType {
    WEB_E2E,
    MOBILE_E2E,
    API,
    LOAD_TEST;

    public Set<ActionType> allowedActions() {
        return switch (this) {
            case API -> Set.of(
                    ActionType.HTTP_REQUEST,
                    ActionType.SET_HEADER,
                    ActionType.SET_QUERY_PARAM,
                    ActionType.SET_AUTH,
                    ActionType.GRAPHQL_QUERY,
                    ActionType.SET_VARIABLE,
                    ActionType.LOG,
                    ActionType.DELAY
            );
            case WEB_E2E -> Set.of(
                    ActionType.NAVIGATE_TO,
                    ActionType.CLICK,
                    ActionType.DOUBLE_CLICK,
                    ActionType.TYPE_TEXT,
                    ActionType.CLEAR_FIELD,
                    ActionType.SELECT_DROPDOWN,
                    ActionType.HOVER,
                    ActionType.SCROLL_TO,
                    ActionType.WAIT_FOR_ELEMENT,
                    ActionType.SWITCH_FRAME,
                    ActionType.SWITCH_WINDOW,
                    ActionType.TAKE_SCREENSHOT,
                    ActionType.ASSERT_VISUAL,
                    ActionType.EXECUTE_JAVASCRIPT,
                    ActionType.UPLOAD_FILE,
                    ActionType.SET_VARIABLE,
                    ActionType.LOG,
                    ActionType.DELAY
            );
            case MOBILE_E2E -> Set.of(
                    ActionType.TAP,
                    ActionType.SWIPE,
                    ActionType.LONG_PRESS,
                    ActionType.MOBILE_SCROLL,
                    ActionType.HIDE_KEYBOARD,
                    ActionType.SET_GEO_LOCATION,
                    ActionType.TYPE_TEXT,
                    ActionType.ASSERT_VISUAL,
                    ActionType.SET_VARIABLE,
                    ActionType.LOG,
                    ActionType.DELAY
            );
            case LOAD_TEST -> Set.of(
                    ActionType.RAMP_UP,
                    ActionType.CONCURRENT_USERS,
                    ActionType.THINK_TIME,
                    ActionType.ASSERT_RESPONSE_TIME,
                    ActionType.CALL_TEST_CASE,
                    ActionType.SET_VARIABLE,
                    ActionType.LOG
            );
        };
    }
}
