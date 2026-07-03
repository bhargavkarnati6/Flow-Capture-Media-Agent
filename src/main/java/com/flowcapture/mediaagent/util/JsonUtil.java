package com.flowcapture.mediaagent.util;

/**
 * JsonUtil holds small, dependency-free JSON helpers shared by the service
 * layer (for parsing inbound payloads) and the model layer (for
 * serializing outbound responses). Keeping this logic in one utility class
 * avoids duplicating string-manipulation code across multiple classes and
 * gives it a single, easily unit-testable home.
 *
 * This is intentionally NOT a general-purpose JSON library — it is just
 * enough token manipulation to extract a flat string field and to escape
 * a string for safe embedding in a JSON response.
 */
public final class JsonUtil {

    private JsonUtil() {
        // Utility class; no instances.
    }

    /**
     * Locates the given key in a flat JSON object and returns the quoted
     * string value that follows it, or null if the key is not present or
     * the JSON is malformed in a way that prevents extraction.
     */
    public static String extractStringValue(String json, String key) {
        if (json == null || key == null) {
            return null;
        }

        String searchToken = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchToken);
        if (keyIndex == -1) {
            return null;
        }

        int colonIndex = json.indexOf(':', keyIndex + searchToken.length());
        if (colonIndex == -1) {
            return null;
        }

        int firstQuote = json.indexOf('"', colonIndex + 1);
        if (firstQuote == -1) {
            return null;
        }

        int secondQuote = json.indexOf('"', firstQuote + 1);
        if (secondQuote == -1) {
            return null;
        }

        return json.substring(firstQuote + 1, secondQuote);
    }

    /**
     * Escapes characters that would otherwise break a JSON string literal.
     * Handles the cases relevant to this application's data: quotes,
     * backslashes, and newlines.
     */
    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
