package com.flowcapture.mediaagent.repository;

import com.flowcapture.mediaagent.exception.AssetIngestionException;
import com.flowcapture.mediaagent.model.MediaAsset;
import com.flowcapture.mediaagent.model.VideoAsset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * These tests exercise AssetDaoImpl against a REAL SQLite database file,
 * created fresh in a JUnit-managed temporary directory for each test. This
 * is intentionally an integration-style test (it touches actual JDBC and
 * actual disk I/O) rather than a mocked unit test, because the DAO's whole
 * job is to correctly translate objects to and from SQL — that behavior is
 * only proven by running it against a real database engine.
 */
class AssetDaoImplTest {

    @TempDir
    Path tempDir;

    private AssetDao assetDao;

    @BeforeEach
    void setUp() throws AssetIngestionException {
        String dbPath = tempDir.resolve("test-media-agent.db").toString();
        assetDao = new AssetDaoImpl(dbPath);
    }

    @Test
    void saveAsset_thenFindById_returnsTheSameAsset() throws AssetIngestionException {
        VideoAsset asset = new VideoAsset("VID-100", "Integration Test Title", "INGESTED",
                "1920x1080", 30.0, "H.264");

        assetDao.saveAsset(asset);
        MediaAsset retrieved = assetDao.findById("VID-100");

        assertNotNull(retrieved);
        assertTrue(retrieved instanceof VideoAsset);
        assertEquals("VID-100", retrieved.getAssetId());
        assertEquals("Integration Test Title", retrieved.getTitle());
        assertEquals("INGESTED", retrieved.getStatus());

        VideoAsset retrievedVideo = (VideoAsset) retrieved;
        assertEquals("1920x1080", retrievedVideo.getResolution());
        assertEquals(30.0, retrievedVideo.getFrameRate());
        assertEquals("H.264", retrievedVideo.getCodec());
    }

    @Test
    void findById_returnsNull_whenAssetDoesNotExist() throws AssetIngestionException {
        MediaAsset result = assetDao.findById("DOES-NOT-EXIST");

        assertNull(result);
    }

    @Test
    void saveAsset_withSameId_updatesExistingRowInsteadOfDuplicating() throws AssetIngestionException {
        VideoAsset original = new VideoAsset("VID-200", "Original Title", "INGESTED",
                "1920x1080", 30.0, "H.264");
        VideoAsset updated = new VideoAsset("VID-200", "Updated Title", "PUBLISHED",
                "3840x2160", 60.0, "H.265");

        assetDao.saveAsset(original);
        assetDao.saveAsset(updated);

        List<MediaAsset> allAssets = assetDao.findAll();
        long matchingRows = allAssets.stream()
                .filter(a -> a.getAssetId().equals("VID-200"))
                .count();

        assertEquals(1, matchingRows, "Saving an asset with an existing id should upsert, not duplicate");

        MediaAsset retrieved = assetDao.findById("VID-200");
        assertEquals("Updated Title", retrieved.getTitle());
        assertEquals("PUBLISHED", retrieved.getStatus());
    }

    @Test
    void findAll_returnsEveryPersistedAsset() throws AssetIngestionException {
        assetDao.saveAsset(new VideoAsset("VID-A", "Title A", "INGESTED", "1920x1080", 30.0, "H.264"));
        assetDao.saveAsset(new VideoAsset("VID-B", "Title B", "INGESTED", "1920x1080", 30.0, "H.264"));
        assetDao.saveAsset(new VideoAsset("VID-C", "Title C", "INGESTED", "1920x1080", 30.0, "H.264"));

        List<MediaAsset> allAssets = assetDao.findAll();

        assertEquals(3, allAssets.size());
    }

    @Test
    void findAll_returnsEmptyList_whenNoAssetsExist() throws AssetIngestionException {
        List<MediaAsset> allAssets = assetDao.findAll();

        assertNotNull(allAssets);
        assertTrue(allAssets.isEmpty());
    }
}
