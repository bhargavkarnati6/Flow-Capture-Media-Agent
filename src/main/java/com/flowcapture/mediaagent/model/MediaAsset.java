package com.flowcapture.mediaagent.model;

/**
 * MediaAsset is the abstract root of the domain model hierarchy.
 *
 * It demonstrates ENCAPSULATION by keeping all state private and exposing
 * controlled access through getters and setters, and it establishes the
 * contract that every concrete media type (video, audio, image, etc.) must
 * fulfil by declaring two abstract methods: getAssetType() and toJson().
 * Because toJson() is abstract here, the API layer can serialize any
 * MediaAsset polymorphically without needing type checks.
 */
public abstract class MediaAsset {

    private String assetId;
    private String title;
    private String status;

    protected MediaAsset(String assetId, String title, String status) {
        this.assetId = assetId;
        this.title = title;
        this.status = status;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Every concrete subclass must declare what kind of media it represents.
     */
    public abstract String getAssetType();

    /**
     * Every concrete subclass must know how to serialize itself to JSON.
     * This keeps serialization logic next to the fields it describes,
     * rather than scattering type-specific formatting code in the API layer.
     */
    public abstract String toJson();

    @Override
    public String toString() {
        return "MediaAsset{" +
                "assetId='" + assetId + '\'' +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
