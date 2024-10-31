package se.sundsvall.messaging.model;

import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record InternalDeliveryResult(String messageId, String deliveryId, MessageType messageType,
	MessageStatus status, String municipalityId) {

	public InternalDeliveryResult(final Message message) {
		this(message.messageId(), message.deliveryId(), message.type(), message.status(), message.municipalityId());
	}

	public InternalDeliveryResult(final Message message, final MessageStatus status) {
		this(message.messageId(), message.deliveryId(), message.type(), status, message.municipalityId());
	}

	public InternalDeliveryResult(final String messageId) {
		this(messageId, null, null, null, null);
	}

	public InternalDeliveryResult(final String messageId, final String deliveryId, final MessageType messageType, final String municipalityId) {
		this(messageId, deliveryId, messageType, null, municipalityId);
	}

}
