package se.sundsvall.messaging.integration.rabbitmq;

/**
 * A request to send an SMS, consumed from {@code api-fabriken.messaging.sms}.
 * <p>
 * This is the contract with postportal and must stay in step with the record of the same name over there.
 * {@code recipientId} is the correlation key: the outcome published back on {@code api-fabriken.messaging.status}
 * carries it, and it is the idempotency key sms-sender is expected to honour.
 */
public record SmsQueueMessage(
	String municipalityId,
	String messageId,
	String recipientId,
	String partyId,
	String mobileNumber,
	String sender,
	String department,
	String message,
	String sentBy,
	String origin)
	implements
	QueueMessage {
}
