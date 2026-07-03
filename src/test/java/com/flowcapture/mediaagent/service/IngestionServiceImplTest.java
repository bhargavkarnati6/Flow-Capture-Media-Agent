package com.flowcapture.mediaagent.service;

import com.flowcapture.mediaagent.exception.AssetIngestionException;
import com.flowcapture.mediaagent.model.MediaAsset;
import com.flowcapture.mediaagent.model.VideoAsset;
import com.flowcapture.mediaagent.repository.AssetDao;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * These tests exercise IngestionServiceImpl.parseVideoAsset(...) directly,
 * with fixed JSON strings standing in for what fetchRawJson(...) would
 * normally return from the network. This keeps the test suite fast and
 * deterministic — it proves the parsing and hydration logic is correct
 * without needing a live HTTP call on every test run.
 *
 * A minimal in-memory fake AssetDao is used purely to satisfy the
 * constructor; these tests never call ingestAsset(...) itself, so the
 * fake's methods are never exercised.
 */
class IngestionServiceImplTest {

    private final IngestionServiceImpl ingestionService = new IngestionServiceImpl(new InMemoryFakeAssetDao());

    @Test
    void parseVideoAsset_hydratesAllFieldsFromJsonAndDefaults() throws AssetIngestionException {
        String rawJson = "{\"userId\":1,\"id\":1,\"title\":\"Sample Movie Title\",\"body\":\"ignored\"}";

        VideoAsset result = ingestionService.parseVideoAsset(rawJson, "VID-1001");

        assertEquals("VID-1001", result.getAssetId());
        assertEquals("Sample Movie Title", result.getTitle());
        assertEquals("INGESTED", result.getStatus());
        assertEquals("VIDEO", result.getAssetType());
        assertEquals("1920x1080", result.getResolution());
        assertEquals(30.0, result.getFrameRate());
        assertEquals("H.264", result.getCodec());
    }

    @Test
    void parseVideoAsset_throwsAssetIngestionException_whenTitleFieldIsMissing() {
        String rawJson = "{\"userId\":1,\"id\":1,\"body\":\"no title here\"}";

        assertThrows(AssetIngestionException.class,
                () -> ingestionService.parseVideoAsset(rawJson, "VID-1001"));
    }

    @Test
    void parseVideoAsset_handlesTitleThatContainsSpecialCharacters() throws AssetIngestionException {
        String rawJson = "{\"id\":1,\"title\":\"A film: the sequel\",\"body\":\"ignored\"}";

        VideoAsset result = ingestionService.parseVideoAsset(rawJson, "VID-2002");

        assertEquals("A film: the sequel", result.getTitle());
    }

    /**
     * A trivial in-memory stand-in for AssetDao, used only to construct
     * IngestionServiceImpl in these tests. It is never actually exercised
     * because these tests call parseVideoAsset(...) directly rather than
     * the full ingestAsset(...) workflow.
     */
    private static class InMemoryFakeAssetDao implements AssetDao {
        private final List<MediaAsset> storage = new ArrayList<>();

        @Override
        public void saveAsset(MediaAsset asset) {
            storage.add(asset);
        }

        @Override
        public MediaAsset findById(String assetId) {
            return storage.stream()
                    .filter(a -> a.getAssetId().equals(assetId))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<MediaAsset> findAll() {
            return storage;
        }
    }
}
