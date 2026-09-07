package se.sundsvall.messaging.integration.rabbitmq;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Routes a failed e-mail request onto this channel's failure hub - either to a wait queue that hands it back after its
 * TTL, or to the dead key, which a direct exchange copies to both the parking lot and the give-up queue.
 * <p>
 * Both the exchange and the dead key belong to e-mail alone. Sharing SMS's would put a copy of every SMS retry in this
 * channel's wait queues, and would hand this channel's give-up events to a listener that reports them as SMS failures.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
public class EmailRetryPublisher {

	private static final Logger LOG = LoggerFactory.getLogger(EmailRetryPublisher.class);

	private final ConfirmedPublisher publisher;
	private final RabbitIntegrationProperties.Flow flow;

	EmailRetryPublisher(final RabbitTemplate rabbitTemplate, final RabbitIntegrationProperties properties) {
		this.publisher = new ConfirmedPublisher(rabbitTemplate, properties.publishConfirmTimeoutSeconds());
		this.flow = properties.email();
	}

	/**
	 * @param nextAttempt the attempt number this republish is for, 1-based and already incremented by the caller
	 */
	public void publishRetry(final EmailQueueMessage message, final int nextAttempt, final String reason, final MessageIds messageIds) {
		final var tier = flow.retryTiers().get(tierIndex(nextAttempt));

		publisher.publish(flow.retryExchange(), tier, message, message.recipientId(),
			Map.of(
				RetryHeaders.ATTEMPT, nextAttempt,
				RetryHeaders.FAILURE_REASON, reason,
				RetryHeaders.MESSAGE_ID, messageIds.messageId(),
				RetryHeaders.BATCH_ID, messageIds.batchId()));

		LOG.info("Republished e-mail for recipient {} on tier {} as attempt {}: {}", message.recipientId(), tier, nextAttempt, reason);
	}

	public void publishGiveUp(final EmailQueueMessage message, final String reason) {
		publisher.publish(flow.retryExchange(), flow.deadRoutingKey(), message, message.recipientId(),
			Map.of(RetryHeaders.FAILURE_REASON, reason));

		LOG.info("Gave up on e-mail for recipient {}: {}", message.recipientId(), reason);
	}

	public boolean hasTierFor(final int nextAttempt) {
		return tierIndex(nextAttempt) < flow.retryTiers().size();
	}

	private static int tierIndex(final int nextAttempt) {
		// Tier 0 carries attempt 2: the first republish waits the shortest tier.
		return nextAttempt - 2;
	}
}
