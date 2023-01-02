package se.sundsvall.messaging.model;

import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record DeliveryResult(String messageId, String deliveryId, MessageStatus status) {

    public DeliveryResult(final Message message) {
        this(message.messageId(), message.deliveryId(), message.status());
    }

    public DeliveryResult(final Message message, final MessageStatus status) {
        this(message.messageId(), message.deliveryId(), status);
    }

    public DeliveryResult(final String messageId) {
        this(messageId, null, null);
    }
}