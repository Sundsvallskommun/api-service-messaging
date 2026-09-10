package se.sundsvall.messaging.integration.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Publishes digital mail outcomes on {@code digital-mail.sent} and {@code digital-mail.failed}. Behaviour lives in
 * {@link OutcomePublisher}; this names the channel's block and its payload record.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
public class DigitalMailOutcomePublisher extends OutcomePublisher {

	DigitalMailOutcomePublisher(final RabbitTemplate rabbitTemplate, final RabbitIntegrationProperties properties) {
		super(rabbitTemplate, properties, properties.digitalMail());
	}

	@Override
	protected DigitalMailStatusMessage toStatusMessage(final String recipientId, final String status, final String externalId, final String statusDetail) {
		return new DigitalMailStatusMessage(recipientId, status, externalId, statusDetail);
	}
}
