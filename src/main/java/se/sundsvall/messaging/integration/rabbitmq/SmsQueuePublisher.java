package se.sundsvall.messaging.integration.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.integration.smssender.SmsDto;

/**
 * Mirrors outgoing SMS onto the RabbitMQ topic exchange as a fire-and-forget side channel.
 * <p>
 * Inert unless {@code rabbitmq.producer.enabled=true}. Publishing never declares topology
 * (the AMQP user has no {@code configure} permission) and never propagates failures - a
 * broken queue must never affect the actual SMS delivery.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.producer.enabled", havingValue = "true")
public class SmsQueuePublisher {

	private static final Logger LOG = LoggerFactory.getLogger(SmsQueuePublisher.class);

	private final RabbitTemplate rabbitTemplate;

	private final RabbitIntegrationProperties properties;

	SmsQueuePublisher(final RabbitTemplate rabbitTemplate, final RabbitIntegrationProperties properties) {
		this.rabbitTemplate = rabbitTemplate;
		this.properties = properties;
	}

	public void publish(final String municipalityId, final SmsDto dto) {
		final var exchange = properties.getExchange();
		final var routingKey = properties.getRoutingKey();

		try {
			final var message = new SmsQueueMessage(municipalityId, dto.sender(), dto.mobileNumber(), dto.message(), dto.priority());
			rabbitTemplate.convertAndSend(exchange, routingKey, message);
			LOG.info("Published SMS to queue (exchange={}, routingKey={})", exchange, routingKey);
		} catch (final Exception e) {
			LOG.warn("Failed to publish SMS to queue (exchange={}, routingKey={})", exchange, routingKey, e);
		}
	}
}
