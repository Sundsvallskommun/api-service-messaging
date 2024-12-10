package se.sundsvall.messaging;

import static se.sundsvall.messaging.model.MessageStatus.PENDING;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;

@Component
class StartupHandler implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(StartupHandler.class);

	private final ApplicationEventPublisher eventPublisher;

	private final DbIntegration dbIntegration;

	StartupHandler(final ApplicationEventPublisher eventPublisher, final DbIntegration dbIntegration) {
		this.eventPublisher = eventPublisher;
		this.dbIntegration = dbIntegration;
	}

	@Override
	public void run(final String... args) {
		final var pendingMessages = dbIntegration.getLatestMessagesWithStatus(PENDING);

		if (pendingMessages.isEmpty()) {
			LOG.info("No pending messages to process");
		} else {
			pendingMessages.stream()
				.map(message -> {
					LOG.info("Processing {} with id {} and delivery id {}", message.getType(),
						message.getMessageId(), message.getDeliveryId());

					return new IncomingMessageEvent(this, message.getMunicipalityId(), message.getType(), message.getDeliveryId(), message.getOrigin());
				})
				.forEach(eventPublisher::publishEvent);
		}
	}

}
