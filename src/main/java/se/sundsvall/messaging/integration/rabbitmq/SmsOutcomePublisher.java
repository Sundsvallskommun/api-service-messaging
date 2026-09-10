package se.sundsvall.messaging.integration.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Publishes SMS outcomes on {@code sms.sent} and {@code sms.failed}. Behaviour lives in
 * {@link OutcomePublisher}; this names the channel's block and its payload record.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
public class SmsOutcomePublisher extends OutcomePublisher {

	SmsOutcomePublisher(final RabbitTemplate rabbitTemplate, final RabbitIntegrationProperties properties) {
		super(rabbitTemplate, properties, properties.sms());
	}

	@Override
	protected SmsStatusMessage toStatusMessage(final String recipientId, final String status, final String externalId, final String statusDetail) {
		return new SmsStatusMessage(recipientId, status, externalId, statusDetail);
	}
}
