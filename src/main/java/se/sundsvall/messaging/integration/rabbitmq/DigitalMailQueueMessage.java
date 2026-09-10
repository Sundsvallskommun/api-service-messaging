package se.sundsvall.messaging.integration.rabbitmq;

import java.util.List;

/**
 * A request to send digital mail, consumed from {@code api-fabriken.messaging.digital-mail}.
 * <p>
 * This is the contract with postportal and must stay in step with the record of the same name over there.
 * {@code recipientId} is the correlation key: the outcome published back on {@code api-fabriken.messaging.status}
 * carries it.
 * <p>
 * One party per message, deliberately. The REST endpoint takes a list because a caller may want one call for many
 * recipients; a queued request is already one recipient's worth of work, and keeping it that way is what lets each one
 * fail, retry and report on its own rather than dragging the others along.
 * <p>
 * Attachments travel as object references rather than as content, for the same reason they do on the e-mail channel:
 * a quorum queue is a poor place to put megabytes, and those bytes would be replicated across three nodes, held in the
 * wait queues for the length of the ladder, and kept in the parking lot after a give-up.
 */
public record DigitalMailQueueMessage(
	String municipalityId,
	String messageId,
	String recipientId,
	String partyId,
	String organizationNumber,
	String subject,
	String body,
	String contentType,
	String department,
	String supportText,
	String supportEmailAddress,
	String supportPhoneNumber,
	String supportUrl,
	List<Attachment> attachments,
	String sentBy,
	String origin)
	implements
	QueueMessage {

	public record Attachment(String filename, String contentType, String objectId) {
	}
}
