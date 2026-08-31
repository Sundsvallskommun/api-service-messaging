package se.sundsvall.messaging.integration.rabbitmq;

/**
 * The single terminal outcome published per send request, onto {@code api-fabriken.messaging.status} with routing key
 * {@code sms.sent} or {@code sms.failed}.
 * <p>
 * The outcome is carried by both the routing key and {@code status}; consumers act on the payload, since dead-lettering
 * their queue rewrites the routing key. {@code externalId} is this service's own message id, so a delivery can be
 * traced back into messaging's history.
 */
public record SmsStatusMessage(
	String recipientId,
	String status,
	String externalId,
	String statusDetail) {
}
