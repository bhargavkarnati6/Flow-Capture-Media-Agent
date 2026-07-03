package com.flowcapture.mediaagent.service;

import com.flowcapture.mediaagent.exception.AssetIngestionException;
import com.flowcapture.mediaagent.model.VideoAsset;

/**
 * IngestionService defines the business-logic contract for retrieving a
 * media asset from a remote source, parsing it into a domain object, and
 * persisting it. Callers (Main, the REST API layer, or a future test)
 * depend only on this interface, never on the concrete implementation.
 */
public interface IngestionService {

    /**
     * Fetches raw media metadata from the given source URL, parses it into
     * a fully hydrated VideoAsset, and persists it through the DAO layer.
     *
     * @param sourceUrl the remote endpoint to fetch metadata from
     * @param assetId   the identifier to assign to the ingested asset
     * @return the fully hydrated, persisted VideoAsset
     * @throws AssetIngestionException if fetching, parsing, or persisting fails
     */
    VideoAsset ingestAsset(String sourceUrl, String assetId) throws AssetIngestionException;
}
