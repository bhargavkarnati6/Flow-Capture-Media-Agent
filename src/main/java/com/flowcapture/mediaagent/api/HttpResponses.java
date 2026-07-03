package com.flowcapture.mediaagent.api;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Small shared helper for writing JSON HTTP responses. Centralizing this
 * avoids duplicating response-writing boilerplate (headers, byte encoding,
 * stream closing) across every handler class.
 */
final class HttpResponses {

    private HttpResponses() {
        // Utility class; no instances.
    }

    static void send(HttpExchange exchange, int statusCode, String jsonBody) throws IOException {
        byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(bytes);
        }
    }
}
