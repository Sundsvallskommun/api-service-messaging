package se.sundsvall.messaging.api.model.request;

/**
 * An attachment whose bytes either travel inline or are named by a reference into the object store.
 * <p>
 * It exists so the one-of rule between the two can be written once. The e-mail request and the e-mail batch request
 * declare structurally identical attachments under a single OpenAPI schema name, so they have to keep agreeing with
 * each other, and a shared type is the cheapest way to be told when they stop.
 */
public interface ReferenceableAttachment {

	String content();

	String objectId();
}
