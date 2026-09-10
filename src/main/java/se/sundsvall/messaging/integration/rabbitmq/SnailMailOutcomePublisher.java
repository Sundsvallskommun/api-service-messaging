package se.sundsvall.messaging.integration.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Publishes snail mail outcomes on {@code snail-mail.sent} and {@code snail-mail.failed}. Behaviour lives in
 * {@link OutcomePublisher}; this names the channel's block and its payload record.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
public class SnailMailOutcomePublisher extends OutcomePublisher {

	SnailMailOutcomePublisher(final RabbitTemplate rabbitTemplate, final RabbitIntegrationProperties properties) {
		super(rabbitTemplate, properties, properties.snailMail());
	}

	@Override
	protected SnailMailStatusMessage toStatusMessage(final String recipientId, final String status, final String externalId, final String statusDetail) {
		return new SnailMailStatusMessage(recipientId, status, externalId, statusDetail);
	}
}
