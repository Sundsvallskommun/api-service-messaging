package se.sundsvall.messaging.integration.rabbitmq;

import java.util.List;

/**
 * A request to send an e-mail, consumed from {@code api-fabriken.messaging.email}.
 * <p>
 * This is the contract with postportal and must stay in step with the record of the same name over there.
 * {@code recipientId} is the correlation key: the outcome published back on {@code api-fabriken.messaging.status}
 * carries it.
 * <p>
 * Attachments travel as object references rather than as content. That is not an optimisation - a quorum queue is a
 * poor place to put megabytes, and the bytes would then be replicated across three nodes, held in the wait queues for
 * the length of the ladder, and kept in the parking lot after a give-up. The reference costs the same whatever the file
 * weighs.
 */
public record EmailQueueMessage(
	String municipalityId,
	String messageId,
	String recipientId,
	String partyId,
	String emailAddress,
	String subject,
	String message,
	String htmlMessage,
	String senderName,
	String senderAddress,
	String replyTo,
	List<Attachment> attachments,
	String sentBy,
	String origin) {

	public record Attachment(String name, String contentType, String objectId) {
	}
}
