package se.sundsvall.messaging.integration.rabbitmq;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static se.sundsvall.messaging.integration.rabbitmq.ConfirmedPublisher.withHeaders;

/**
 * Publishes the one terminal outcome per send request onto the outcome hub.
 * <p>
 * {@code sms.sent} means sms-sender accepted the request - accepted by the gateway, not delivered to a handset.
 * {@code sms.failed} means the request has been given up on and nothing further will be attempted.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
public class SmsOutcomePublisher {

	static final String SENT = "SENT";
	static final String FAILED = "FAILED";

	private static final Logger LOG = LoggerFactory.getLogger(SmsOutcomePublisher.class);

	private final ConfirmedPublisher publisher;
	private final RabbitIntegrationProperties properties;

	SmsOutcomePublisher(final RabbitTemplate rabbitTemplate, final RabbitIntegrationProperties properties) {
		this.publisher = new ConfirmedPublisher(rabbitTemplate, properties.getPublishConfirmTimeoutSeconds());
		this.properties = properties;
	}

	public void publishSent(final String recipientId, final String messageId) {
		publish(properties.getSentRoutingKey(), new SmsStatusMessage(recipientId, SENT, messageId, null));
	}

	public void publishFailed(final String recipientId, final String statusDetail) {
		publish(properties.getFailedRoutingKey(), new SmsStatusMessage(recipientId, FAILED, null, statusDetail));
	}

	private void publish(final String routingKey, final SmsStatusMessage outcome) {
		publisher.publish(properties.getStatusExchange(), routingKey, outcome, outcome.recipientId(), withHeaders(Map.of()));

		LOG.info("Published outcome {} for recipient {} (exchange={}, routingKey={})",
			outcome.status(), outcome.recipientId(), properties.getStatusExchange(), routingKey);
	}
}
