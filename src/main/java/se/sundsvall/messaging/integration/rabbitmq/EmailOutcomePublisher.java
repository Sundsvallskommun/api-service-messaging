package se.sundsvall.messaging.integration.rabbitmq;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Publishes the one terminal outcome per send request onto the outcome hub.
 * <p>
 * {@code email.sent} means email-sender accepted the request - accepted for delivery, not read by anyone.
 * {@code email.failed} means the request has been given up on and nothing further will be attempted.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
public class EmailOutcomePublisher {

	static final String SENT = "SENT";
	static final String FAILED = "FAILED";

	private static final Logger LOG = LoggerFactory.getLogger(EmailOutcomePublisher.class);

	private final ConfirmedPublisher publisher;
	private final RabbitIntegrationProperties.Flow flow;

	EmailOutcomePublisher(final RabbitTemplate rabbitTemplate, final RabbitIntegrationProperties properties) {
		this.publisher = new ConfirmedPublisher(rabbitTemplate, properties.publishConfirmTimeoutSeconds());
		this.flow = properties.email();
	}

	public void publishSent(final String recipientId, final String messageId) {
		publish(flow.sentRoutingKey(), new EmailStatusMessage(recipientId, SENT, messageId, null));
	}

	public void publishFailed(final String recipientId, final String statusDetail) {
		publish(flow.failedRoutingKey(), new EmailStatusMessage(recipientId, FAILED, null, statusDetail));
	}

	private void publish(final String routingKey, final EmailStatusMessage outcome) {
		publisher.publish(flow.statusExchange(), routingKey, outcome, outcome.recipientId(), Map.of());

		LOG.info("Published outcome {} for recipient {} (exchange={}, routingKey={})",
			outcome.status(), outcome.recipientId(), flow.statusExchange(), routingKey);
	}
}
