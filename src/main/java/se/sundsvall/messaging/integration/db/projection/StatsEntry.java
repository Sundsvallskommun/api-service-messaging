package se.sundsvall.messaging.integration.db.projection;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

public record StatsEntry(
	MessageType messageType,
	MessageType originalMessageType,
	MessageStatus status,
	String origin,
	String department,
	String municipalityId) {

	public StatsEntry(final MessageType messageType, final MessageType originalMessageType, final MessageStatus status, final String municipalityId) {
		this(messageType, originalMessageType, status, null, null, municipalityId);
	}

}
