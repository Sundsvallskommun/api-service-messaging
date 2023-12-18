package se.sundsvall.messaging.service.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import se.sundsvall.messaging.model.MessageType;

@Getter
public final class IncomingMessageEvent extends ApplicationEvent {

	private static final long serialVersionUID = -5961606738254317475L;

	private final MessageType messageType;
	private final String deliveryId;

	public IncomingMessageEvent(final Object source, final MessageType messageType,
		final String deliveryId) {
		super(source);

		this.messageType = messageType;
		this.deliveryId = deliveryId;
	}
}
