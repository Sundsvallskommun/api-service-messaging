package se.sundsvall.messaging.model;

import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record InternalDeliveryResult(String messageId, String deliveryId, MessageType messageType, MessageStatus status) {

    public InternalDeliveryResult(final Message message) {
        this(message.messageId(), message.deliveryId(), message.type(), message.status());
    }

    public InternalDeliveryResult(final Message message, final MessageStatus status) {
        this(message.messageId(), message.deliveryId(), message.type(), status);
    }

    public InternalDeliveryResult(final String messageId) {
        this(messageId, null, null, null);
    }
}