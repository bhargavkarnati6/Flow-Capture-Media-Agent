package com.flowcapture.mediaagent.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JsonUtilTest {

    @Test
    void extractStringValue_returnsValue_whenKeyIsPresent() {
        String json = "{\"userId\":1,\"id\":1,\"title\":\"sample title\",\"body\":\"sample body\"}";

        String result = JsonUtil.extractStringValue(json, "title");

        assertEquals("sample title", result);
    }

    @Test
    void extractStringValue_returnsNull_whenKeyIsAbsent() {
        String json = "{\"userId\":1,\"id\":1,\"body\":\"sample body\"}";

        String result = JsonUtil.extractStringValue(json, "title");

        assertNull(result);
    }

    @Test
    void extractStringValue_returnsNull_whenJsonIsNull() {
        assertNull(JsonUtil.extractStringValue(null, "title"));
    }

    @Test
    void extractStringValue_findsCorrectKey_whenMultipleKeysArePresent() {
        String json = "{\"id\":1,\"otherTitle\":\"wrong\",\"title\":\"correct\"}";

        String result = JsonUtil.extractStringValue(json, "title");

        assertEquals("correct", result);
    }

    @Test
    void escape_escapesQuotesAndBackslashes() {
        String raw = "he said \"hi\" and used a \\backslash";

        String escaped = JsonUtil.escape(raw);

        assertEquals("he said \\\"hi\\\" and used a \\\\backslash", escaped);
    }

    @Test
    void escape_returnsEmptyString_whenValueIsNull() {
        assertEquals("", JsonUtil.escape(null));
    }
}
