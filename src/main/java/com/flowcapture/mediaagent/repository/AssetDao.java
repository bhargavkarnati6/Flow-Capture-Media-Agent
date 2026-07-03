package com.flowcapture.mediaagent.repository;

import com.flowcapture.mediaagent.exception.AssetIngestionException;
import com.flowcapture.mediaagent.model.MediaAsset;

import java.util.List;

/**
 * AssetDao defines the persistence contract for MediaAsset objects.
 *
 * This is an example of INTERFACE SEGREGATION: the service and API layers
 * only depend on this narrow, purpose-built contract and never need to
 * know whether the underlying storage is SQLite, PostgreSQL, or something
 * else entirely. Swapping AssetDaoImpl for a different JDBC-backed engine
 * would require zero changes to any calling code.
 */
public interface AssetDao {

    /**
     * Persists the given asset, inserting a new row or updating the
     * existing row if an asset with the same id already exists.
     *
     * @param asset the asset to persist
     * @throws AssetIngestionException if the asset cannot be written
     */
    void saveAsset(MediaAsset asset) throws AssetIngestionException;

    /**
     * Looks up a previously persisted asset by its unique identifier.
     *
     * @param assetId the unique identifier of the asset
     * @return the matching MediaAsset, or null if no match was found
     * @throws AssetIngestionException if the underlying storage cannot be read
     */
    MediaAsset findById(String assetId) throws AssetIngestionException;

    /**
     * Returns every persisted asset, ordered by asset id.
     *
     * @return a list of all persisted assets (empty if none exist)
     * @throws AssetIngestionException if the underlying storage cannot be read
     */
    List<MediaAsset> findAll() throws AssetIngestionException;
}
