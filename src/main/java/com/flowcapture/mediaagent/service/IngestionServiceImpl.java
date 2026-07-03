package com.flowcapture.mediaagent.service;

import com.flowcapture.mediaagent.exception.AssetIngestionException;
import com.flowcapture.mediaagent.model.VideoAsset;
import com.flowcapture.mediaagent.repository.AssetDao;
import com.flowcapture.mediaagent.util.JsonUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * IngestionServiceImpl is the concrete business-logic component that talks
 * to the outside world. It uses the native java.net.http.HttpClient
 * (no third-party dependencies for the HTTP call itself) to send an
 * ASYNCHRONOUS GET request to a public endpoint, then parses the response
 * using the shared JsonUtil token-extraction helper.
 *
 * The public endpoint used by default (jsonplaceholder.typicode.com) only
 * returns generic fields (id, title, body). Since a real video-ingestion
 * pipeline would receive genuine codec/resolution/frame-rate metadata from
 * an internal media API, this class enriches the parsed record with
 * sensible default video metadata to keep the domain model fully hydrated.
 */
public class IngestionServiceImpl implements IngestionService {

    private final AssetDao assetDao;
    private final HttpClient httpClient;

    public IngestionServiceImpl(AssetDao assetDao) {
        this.assetDao = assetDao;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public VideoAsset ingestAsset(String sourceUrl, String assetId) throws AssetIngestionException {
        String rawJson = fetchRawJson(sourceUrl);
        VideoAsset videoAsset = parseVideoAsset(rawJson, assetId);
        assetDao.saveAsset(videoAsset);
        return videoAsset;
    }

    /**
     * Sends an asynchronous GET request and blocks only at the very end to
     * retrieve the result, demonstrating the async HttpClient API while
     * still returning a simple, synchronous method signature to the caller.
     */
    private String fetchRawJson(String sourceUrl) throws AssetIngestionException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(sourceUrl))
                .GET()
                .build();

        CompletableFuture<HttpResponse<String>> futureResponse =
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        try {
            HttpResponse<String> response = futureResponse.get();
            if (response.statusCode() != 200) {
                throw new AssetIngestionException(
                        "Ingestion source returned non-success HTTP status: " + response.statusCode());
            }
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssetIngestionException("Ingestion request was interrupted for URL: " + sourceUrl, e);
        } catch (ExecutionException e) {
            throw new AssetIngestionException("Ingestion request failed for URL: " + sourceUrl, e.getCause());
        }
    }

    /**
     * Extracts the "title" field from the raw JSON text and builds a fully
     * hydrated VideoAsset by combining the fetched title with standard
     * video metadata defaults.
     *
     * Package-private (no access modifier) on purpose: this lets the test
     * suite exercise the parsing logic directly with fixed JSON strings,
     * without needing a real network call for every test run.
     */
    VideoAsset parseVideoAsset(String rawJson, String assetId) throws AssetIngestionException {
        String title = JsonUtil.extractStringValue(rawJson, "title");

        if (title == null) {
            throw new AssetIngestionException("Unable to locate 'title' field in ingestion response payload");
        }

        String status = "INGESTED";
        String resolution = "1920x1080";
        double frameRate = 30.0;
        String codec = "H.264";

        return new VideoAsset(assetId, title, status, resolution, frameRate, codec);
    }
}
