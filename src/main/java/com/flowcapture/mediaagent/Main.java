package com.flowcapture.mediaagent;

import com.flowcapture.mediaagent.api.AssetApiServer;
import com.flowcapture.mediaagent.exception.AssetIngestionException;
import com.flowcapture.mediaagent.model.VideoAsset;
import com.flowcapture.mediaagent.repository.AssetDao;
import com.flowcapture.mediaagent.repository.AssetDaoImpl;
import com.flowcapture.mediaagent.service.IngestionService;
import com.flowcapture.mediaagent.service.IngestionServiceImpl;

import java.io.IOException;

/**
 * Main is the composition root of the application. It is the only class
 * that knows about concrete implementations (AssetDaoImpl,
 * IngestionServiceImpl, AssetApiServer); every other class in the codebase
 * depends only on interfaces. This is dependency injection performed by
 * hand, without a framework, and it is what keeps the tiers cleanly
 * separated and independently testable.
 *
 * On startup, Main seeds the database with one ingested asset so that
 * GET /assets returns something immediately, then starts the REST API
 * server so the workflow can also be triggered live over HTTP.
 */
public class Main {

    private static final String SEED_SOURCE_URL = "https://jsonplaceholder.typicode.com/posts/1";
    private static final String SEED_ASSET_ID = "VID-1001";
    private static final int API_PORT = 8080;

    public static void main(String[] args) {
        System.out.println("[Flow Capture Media Agent] Starting up...");

        try {
            AssetDao assetDao = new AssetDaoImpl();
            IngestionService ingestionService = new IngestionServiceImpl(assetDao);

            System.out.println("[Main] DAO and Service tiers wired successfully (SQLite-backed persistence).");

            runSeedIngestion(ingestionService, assetDao);

            AssetApiServer apiServer = new AssetApiServer(API_PORT, assetDao, ingestionService);
            apiServer.start();

            System.out.println("[Main] Server is running. Try these in another terminal:");
            System.out.println("       curl http://localhost:" + API_PORT + "/assets");
            System.out.println("       curl http://localhost:" + API_PORT + "/assets/" + SEED_ASSET_ID);
            System.out.println("       curl -X POST \"http://localhost:" + API_PORT
                    + "/ingest?sourceUrl=https://jsonplaceholder.typicode.com/posts/2&assetId=VID-1002\"");
            System.out.println("[Main] Press Ctrl+C to stop the server.");

        } catch (AssetIngestionException e) {
            System.err.println("[Main] Startup failed during ingestion or persistence setup: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("[Main] Root cause: " + e.getCause());
            }
        } catch (IOException e) {
            System.err.println("[Main] Failed to start the REST API server on port " + API_PORT + ": " + e.getMessage());
        }
    }

    private static void runSeedIngestion(IngestionService ingestionService, AssetDao assetDao) {
        System.out.println("[Main] Seeding database with one ingested asset from: " + SEED_SOURCE_URL);
        try {
            VideoAsset ingestedAsset = ingestionService.ingestAsset(SEED_SOURCE_URL, SEED_ASSET_ID);
            System.out.println("[Main] Seed ingestion succeeded: " + ingestedAsset);
        } catch (AssetIngestionException e) {
            System.err.println("[Main] Seed ingestion failed (server will still start): " + e.getMessage());
        }
    }
}
