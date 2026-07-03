package com.flowcapture.mediaagent.model;

import com.flowcapture.mediaagent.util.JsonUtil;

/**
 * VideoAsset extends MediaAsset to demonstrate INHERITANCE.
 *
 * It reuses the identity and lifecycle fields defined by the parent
 * (assetId, title, status) and layers on video-specific metadata that only
 * makes sense for this concrete media type.
 */
public class VideoAsset extends MediaAsset {

    private String resolution;
    private double frameRate;
    private String codec;

    public VideoAsset(String assetId, String title, String status,
                       String resolution, double frameRate, String codec) {
        super(assetId, title, status);
        this.resolution = resolution;
        this.frameRate = frameRate;
        this.codec = codec;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public double getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(double frameRate) {
        this.frameRate = frameRate;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    @Override
    public String getAssetType() {
        return "VIDEO";
    }

    @Override
    public String toJson() {
        return "{"
                + "\"assetId\":\"" + JsonUtil.escape(getAssetId()) + "\","
                + "\"title\":\"" + JsonUtil.escape(getTitle()) + "\","
                + "\"status\":\"" + JsonUtil.escape(getStatus()) + "\","
                + "\"assetType\":\"" + JsonUtil.escape(getAssetType()) + "\","
                + "\"resolution\":\"" + JsonUtil.escape(resolution) + "\","
                + "\"frameRate\":" + frameRate + ","
                + "\"codec\":\"" + JsonUtil.escape(codec) + "\""
                + "}";
    }

    @Override
    public String toString() {
        return "VideoAsset{" +
                "assetId='" + getAssetId() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", status='" + getStatus() + '\'' +
                ", resolution='" + resolution + '\'' +
                ", frameRate=" + frameRate +
                ", codec='" + codec + '\'' +
                '}';
    }
}
