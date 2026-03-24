package se.sundsvall.messaging.model;

public record MessageOutcome(MessageStatus status, String transactionId) {

	public MessageOutcome(final MessageStatus status) {
		this(status, null);
	}
}
