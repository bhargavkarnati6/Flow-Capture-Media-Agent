package com.flowcapture.mediaagent.api;

import com.flowcapture.mediaagent.repository.AssetDao;
import com.flowcapture.mediaagent.service.IngestionService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * AssetApiServer wires the REST endpoints to the JDK's built-in
 * com.sun.net.httpserver.HttpServer. No web framework (Spring, Javalin,
 * etc.) is used here on purpose: the goal is to demonstrate that this
 * application genuinely serves HTTP requests, using nothing beyond the
 * standard library, in the same dependency-free spirit as the rest of
 * the codebase.
 */
public class AssetApiServer {

    private final HttpServer server;
    private final int port;

    public AssetApiServer(int port, AssetDao assetDao, IngestionService ingestionService) throws IOException {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/assets", new AssetsHandler(assetDao));
        this.server.createContext("/ingest", new IngestHandler(ingestionService));
        this.server.setExecutor(null);
    }

    public void start() {
        server.start();
        System.out.println("[AssetApiServer] Listening on http://localhost:" + port);
        System.out.println("[AssetApiServer]   GET  /assets           -> list all persisted assets");
        System.out.println("[AssetApiServer]   GET  /assets/{id}       -> fetch a single asset by id");
        System.out.println("[AssetApiServer]   POST /ingest?sourceUrl=...&assetId=... -> ingest a new asset");
    }

    public void stop() {
        server.stop(0);
    }
}
