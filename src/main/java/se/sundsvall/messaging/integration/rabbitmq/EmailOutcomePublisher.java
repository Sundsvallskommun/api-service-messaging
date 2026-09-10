package se.sundsvall.messaging.integration.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Publishes e-mail outcomes on {@code email.sent} and {@code email.failed}. Behaviour lives in
 * {@link OutcomePublisher}; this names the channel's block and its payload record.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
public class EmailOutcomePublisher extends OutcomePublisher {

	EmailOutcomePublisher(final RabbitTemplate rabbitTemplate, final RabbitIntegrationProperties properties) {
		super(rabbitTemplate, properties, properties.email());
	}

	@Override
	protected EmailStatusMessage toStatusMessage(final String recipientId, final String status, final String externalId, final String statusDetail) {
		return new EmailStatusMessage(recipientId, status, externalId, statusDetail);
	}
}
