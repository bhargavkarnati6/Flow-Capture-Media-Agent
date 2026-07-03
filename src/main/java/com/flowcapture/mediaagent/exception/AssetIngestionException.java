package com.flowcapture.mediaagent.exception;

/**
 * AssetIngestionException is a CHECKED exception representing any failure
 * that occurs while fetching, parsing, persisting, or serving a media asset.
 *
 * It is deliberately checked (extends Exception, not RuntimeException) so
 * that callers are forced by the compiler to acknowledge and handle
 * failures, which is the correct posture for a workflow that touches the
 * network, the database, and the file system.
 */
public class AssetIngestionException extends Exception {

    public AssetIngestionException(String message) {
        super(message);
    }

    public AssetIngestionException(String message, Throwable cause) {
        super(message, cause);
    }
}
