package com.flowcapture.mediaagent.api;

import com.flowcapture.mediaagent.exception.AssetIngestionException;
import com.flowcapture.mediaagent.model.VideoAsset;
import com.flowcapture.mediaagent.service.IngestionService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * IngestHandler exposes the ingestion workflow over HTTP:
 *
 *   POST /ingest?sourceUrl=<url>&assetId=<id>
 *
 * This is what turns the application from something that only consumes an
 * external API into something that also serves its own API — a caller
 * (a frontend, a script, curl, Postman) can trigger a real ingestion
 * workflow without touching the JVM process directly.
 */
public class IngestHandler implements HttpHandler {

    private final IngestionService ingestionService;

    public IngestHandler(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.send(exchange, 405, "{\"error\":\"Only POST is supported on /ingest\"}");
            return;
        }

        Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
        String sourceUrl = params.get("sourceUrl");
        String assetId = params.get("assetId");

        if (sourceUrl == null || assetId == null) {
            HttpResponses.send(exchange, 400,
                    "{\"error\":\"Both 'sourceUrl' and 'assetId' query parameters are required\"}");
            return;
        }

        try {
            VideoAsset asset = ingestionService.ingestAsset(sourceUrl, assetId);
            HttpResponses.send(exchange, 201, asset.toJson());
        } catch (AssetIngestionException e) {
            HttpResponses.send(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Map<String, String> parseQueryParams(String rawQuery) {
        Map<String, String> params = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return params;
        }
        for (String pair : rawQuery.split("&")) {
            int equalsIndex = pair.indexOf('=');
            if (equalsIndex > 0) {
                String key = pair.substring(0, equalsIndex);
                String value = pair.substring(equalsIndex + 1);
                params.put(key, value);
            }
        }
        return params;
    }
}
