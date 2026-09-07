package se.sundsvall.messaging.integration.objectstore;

/**
 * An object read from the object store: its bytes, plus what the store says it is. The content type and file name are
 * what the object was stored with and are only ever fallbacks - an attachment may name its own, and does so whenever
 * the sender knows better than whoever uploaded the file.
 */
public record StoredObject(byte[] content, String contentType, String fileName) {
}
