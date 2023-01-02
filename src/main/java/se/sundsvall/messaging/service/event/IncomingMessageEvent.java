package se.sundsvall.messaging.service.event;

import org.springframework.context.ApplicationEvent;

import se.sundsvall.messaging.model.MessageType;

import lombok.Getter;

@Getter
public final class IncomingMessageEvent extends ApplicationEvent {

    private final MessageType messageType;
    private final String deliveryId;

    public IncomingMessageEvent(final Object source, final MessageType messageType,
            final String deliveryId) {
        super(source);

        this.messageType = messageType;
        this.deliveryId = deliveryId;
    }
}
