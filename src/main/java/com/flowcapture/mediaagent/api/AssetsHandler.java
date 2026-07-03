package com.flowcapture.mediaagent.api;

import com.flowcapture.mediaagent.exception.AssetIngestionException;
import com.flowcapture.mediaagent.model.MediaAsset;
import com.flowcapture.mediaagent.repository.AssetDao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;

/**
 * AssetsHandler serves read access to persisted assets over HTTP:
 *
 *   GET /assets       -> a JSON array of every persisted asset
 *   GET /assets/{id}   -> a single asset as JSON, or 404 if not found
 *
 * This class depends only on the AssetDao interface, not on AssetDaoImpl,
 * which keeps the API layer decoupled from the persistence mechanism in
 * exactly the same way the service layer is.
 */
public class AssetsHandler implements HttpHandler {

    private final AssetDao assetDao;

    public AssetsHandler(AssetDao assetDao) {
        this.assetDao = assetDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.send(exchange, 405, "{\"error\":\"Only GET is supported on /assets\"}");
            return;
        }

        String path = exchange.getRequestURI().getPath();

        try {
            if (path.equals("/assets") || path.equals("/assets/")) {
                handleListAll(exchange);
            } else {
                handleFindById(exchange, path);
            }
        } catch (AssetIngestionException e) {
            HttpResponses.send(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleListAll(HttpExchange exchange) throws IOException, AssetIngestionException {
        List<MediaAsset> assets = assetDao.findAll();

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < assets.size(); i++) {
            json.append(assets.get(i).toJson());
            if (i < assets.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");

        HttpResponses.send(exchange, 200, json.toString());
    }

    private void handleFindById(HttpExchange exchange, String path) throws IOException, AssetIngestionException {
        String assetId = path.substring(path.lastIndexOf('/') + 1);
        MediaAsset asset = assetDao.findById(assetId);

        if (asset == null) {
            HttpResponses.send(exchange, 404, "{\"error\":\"No asset found with id '" + assetId + "'\"}");
        } else {
            HttpResponses.send(exchange, 200, asset.toJson());
        }
    }
}
