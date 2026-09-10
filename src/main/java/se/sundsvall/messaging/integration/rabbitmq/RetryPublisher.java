package se.sundsvall.messaging.integration.rabbitmq;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Routes a failed send request onto its own channel's failure hub - either to a wait queue that hands it back after its
 * TTL, or to the dead key, which a direct exchange copies to both the parking lot and the give-up queue.
 * <p>
 * Both the exchange and the dead key belong to one channel alone, which is why this class is parameterised by
 * {@link RabbitIntegrationProperties.Flow} rather than reading one fixed block. Sharing another channel's exchange
 * would put a copy of every one of its retries in these wait queues, and would hand this channel's give-up events to a
 * listener that reports them as that channel's failures.
 *
 * @param <T> this channel's queue payload
 */
public abstract class RetryPublisher<T extends QueueMessage> {

	private static final Logger LOG = LoggerFactory.getLogger(RetryPublisher.class);

	private final ConfirmedPublisher publisher;
	private final RabbitIntegrationProperties.Flow flow;
	private final String channel;

	/**
	 * @param channel the channel's name as it should read in a log line, e.g. {@code "e-mail"}
	 */
	protected RetryPublisher(final RabbitTemplate rabbitTemplate, final RabbitIntegrationProperties properties,
		final RabbitIntegrationProperties.Flow flow, final String channel) {
		this.publisher = new ConfirmedPublisher(rabbitTemplate, properties.publishConfirmTimeoutSeconds());
		this.flow = flow;
		this.channel = channel;
	}

	/**
	 * @param nextAttempt the attempt number this republish is for, 1-based and already incremented by the caller
	 */
	public void publishRetry(final T message, final int nextAttempt, final String reason, final MessageIds messageIds) {
		final var tier = flow.retryTiers().get(tierIndex(nextAttempt));

		publisher.publish(flow.retryExchange(), tier, message, message.recipientId(),
			Map.of(
				RetryHeaders.ATTEMPT, nextAttempt,
				RetryHeaders.FAILURE_REASON, reason,
				RetryHeaders.MESSAGE_ID, messageIds.messageId(),
				RetryHeaders.BATCH_ID, messageIds.batchId()));

		LOG.info("Republished {} for recipient {} on tier {} as attempt {}: {}", channel, message.recipientId(), tier, nextAttempt, reason);
	}

	public void publishGiveUp(final T message, final String reason) {
		publisher.publish(flow.retryExchange(), flow.deadRoutingKey(), message, message.recipientId(),
			Map.of(RetryHeaders.FAILURE_REASON, reason));

		LOG.info("Gave up on {} for recipient {}: {}", channel, message.recipientId(), reason);
	}

	public boolean hasTierFor(final int nextAttempt) {
		return tierIndex(nextAttempt) < flow.retryTiers().size();
	}

	private static int tierIndex(final int nextAttempt) {
		// Tier 0 carries attempt 2: the first republish waits the shortest tier.
		return nextAttempt - 2;
	}
}
