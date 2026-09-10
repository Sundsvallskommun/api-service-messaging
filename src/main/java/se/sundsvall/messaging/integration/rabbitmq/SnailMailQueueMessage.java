package se.sundsvall.messaging.integration.rabbitmq;

import java.util.List;

/**
 * A request to send snail mail, consumed from {@code api-fabriken.messaging.snail-mail}.
 * <p>
 * This is the contract with postportal and must stay in step with the record of the same name over there.
 * {@code recipientId} is the correlation key: the outcome published back on {@code api-fabriken.messaging.status}
 * carries it.
 * <p>
 * {@code batchId} is postportal's message id, which is what it has always sent as the batch identity over REST. It
 * groups the recipients of one letter for snailmail-sender, which flushes the batch on its own schedule - so it is a
 * grouping key here, not a coordination point, and nothing on this side waits for a batch to be complete.
 * <p>
 * Attachments travel as object references rather than as content. That matters more here than on any other channel:
 * the attachments <em>are</em> the letter, so inlining them would put the whole document on the queue, replicated
 * across three nodes and kept through every retry tier.
 */
public record SnailMailQueueMessage(
	String municipalityId,
	String messageId,
	String recipientId,
	String batchId,
	String partyId,
	String department,
	String folderName,
	String deviation,
	Address address,
	List<Attachment> attachments,
	String sentBy,
	String origin)
	implements
	QueueMessage {

	public record Address(
		String firstName,
		String lastName,
		String organizationName,
		String address,
		String apartmentNumber,
		String careOf,
		String zipCode,
		String city,
		String country) {
	}

	public record Attachment(String filename, String contentType, String objectId) {
	}
}
