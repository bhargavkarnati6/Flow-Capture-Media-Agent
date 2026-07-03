package com.flowcapture.mediaagent.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VideoAssetTest {

    @Test
    void constructor_hydratesAllFieldsCorrectly() {
        VideoAsset asset = new VideoAsset("VID-1", "Test Title", "INGESTED", "1920x1080", 30.0, "H.264");

        assertEquals("VID-1", asset.getAssetId());
        assertEquals("Test Title", asset.getTitle());
        assertEquals("INGESTED", asset.getStatus());
        assertEquals("1920x1080", asset.getResolution());
        assertEquals(30.0, asset.getFrameRate());
        assertEquals("H.264", asset.getCodec());
    }

    @Test
    void getAssetType_alwaysReturnsVideo() {
        VideoAsset asset = new VideoAsset("VID-1", "Test", "INGESTED", "1920x1080", 30.0, "H.264");

        assertEquals("VIDEO", asset.getAssetType());
    }

    @Test
    void setters_updateFieldsThroughEncapsulatedAccessors() {
        VideoAsset asset = new VideoAsset("VID-1", "Test", "INGESTED", "1920x1080", 30.0, "H.264");

        asset.setTitle("Updated Title");
        asset.setStatus("PUBLISHED");
        asset.setResolution("3840x2160");
        asset.setFrameRate(60.0);
        asset.setCodec("H.265");

        assertEquals("Updated Title", asset.getTitle());
        assertEquals("PUBLISHED", asset.getStatus());
        assertEquals("3840x2160", asset.getResolution());
        assertEquals(60.0, asset.getFrameRate());
        assertEquals("H.265", asset.getCodec());
    }

    @Test
    void toJson_containsAllFieldsWithCorrectKeys() {
        VideoAsset asset = new VideoAsset("VID-1", "Test Title", "INGESTED", "1920x1080", 30.0, "H.264");

        String json = asset.toJson();

        assertTrue(json.contains("\"assetId\":\"VID-1\""));
        assertTrue(json.contains("\"title\":\"Test Title\""));
        assertTrue(json.contains("\"status\":\"INGESTED\""));
        assertTrue(json.contains("\"assetType\":\"VIDEO\""));
        assertTrue(json.contains("\"resolution\":\"1920x1080\""));
        assertTrue(json.contains("\"frameRate\":30.0"));
        assertTrue(json.contains("\"codec\":\"H.264\""));
    }

    @Test
    void toJson_escapesQuotesInTitle() {
        VideoAsset asset = new VideoAsset("VID-1", "A \"Quoted\" Title", "INGESTED", "1920x1080", 30.0, "H.264");

        String json = asset.toJson();

        assertTrue(json.contains("\\\"Quoted\\\""));
    }
}
