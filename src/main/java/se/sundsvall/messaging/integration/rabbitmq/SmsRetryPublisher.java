package se.sundsvall.messaging.integration.rabbitmq;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Routes a failed request onto the failure hub - either to a wait queue that will hand it back after its TTL, or to the
 * {@code dead} key, which a direct exchange copies to both the parking lot and the give-up queue.
 * <p>
 * The attempt counter rides on the message as a header, so it survives the trip through a wait queue and back.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
public class SmsRetryPublisher {

	public static final String ATTEMPT_HEADER = "x-attempt";
	public static final String FAILURE_REASON_HEADER = "x-failure-reason";
	/**
	 * Messaging's own ids, carried so every attempt at one SMS shares them. Not to be confused with
	 * {@link SmsQueueMessage#messageId()}, which is postportal's.
	 */
	public static final String MESSAGE_ID_HEADER = "x-message-id";
	public static final String BATCH_ID_HEADER = "x-batch-id";

	private static final Logger LOG = LoggerFactory.getLogger(SmsRetryPublisher.class);

	private final ConfirmedPublisher publisher;
	private final RabbitIntegrationProperties properties;

	SmsRetryPublisher(final RabbitTemplate rabbitTemplate, final RabbitIntegrationProperties properties) {
		this.publisher = new ConfirmedPublisher(rabbitTemplate, properties.getPublishConfirmTimeoutSeconds());
		this.properties = properties;
	}

	/**
	 * @param nextAttempt the attempt number this republish is for, 1-based and already incremented by the caller
	 */
	public void publishRetry(final SmsQueueMessage message, final int nextAttempt, final String reason, final MessageIds messageIds) {
		final var tier = properties.getRetryTiers().get(tierIndex(nextAttempt));

		publisher.publish(properties.getRetryExchange(), tier, message, message.recipientId(),
			Map.of(
				ATTEMPT_HEADER, nextAttempt,
				FAILURE_REASON_HEADER, reason,
				MESSAGE_ID_HEADER, messageIds.messageId(),
				BATCH_ID_HEADER, messageIds.batchId()));

		LOG.info("Republished SMS for recipient {} on tier {} as attempt {}: {}", message.recipientId(), tier, nextAttempt, reason);
	}

	public void publishGiveUp(final SmsQueueMessage message, final String reason) {
		publisher.publish(properties.getRetryExchange(), properties.getDeadRoutingKey(), message, message.recipientId(),
			Map.of(FAILURE_REASON_HEADER, reason));

		LOG.info("Gave up on SMS for recipient {}: {}", message.recipientId(), reason);
	}

	public boolean hasTierFor(final int nextAttempt) {
		return tierIndex(nextAttempt) < properties.getRetryTiers().size();
	}

	private static int tierIndex(final int nextAttempt) {
		// Tier 0 carries attempt 2: the first republish waits the shortest tier.
		return nextAttempt - 2;
	}

	/**
	 * Messaging's own message and batch ids for one logical SMS, held together so an attempt can hand them to the next.
	 */
	public record MessageIds(String messageId, String batchId) {}
}
