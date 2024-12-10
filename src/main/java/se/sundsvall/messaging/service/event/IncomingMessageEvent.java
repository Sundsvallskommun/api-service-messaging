package se.sundsvall.messaging.service.event;

import java.io.Serial;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import se.sundsvall.messaging.model.MessageType;

@Getter
public final class IncomingMessageEvent extends ApplicationEvent {

	@Serial
	private static final long serialVersionUID = -5961606738254317475L;

	private final String municipalityId;

	private final MessageType messageType;

	private final String deliveryId;

	private final String origin;

	public IncomingMessageEvent(final Object source, final String municipalityId, final MessageType messageType,
		final String deliveryId, final String origin) {
		super(source);

		this.messageType = messageType;
		this.deliveryId = deliveryId;
		this.origin = origin;
		this.municipalityId = municipalityId;

	}

}
