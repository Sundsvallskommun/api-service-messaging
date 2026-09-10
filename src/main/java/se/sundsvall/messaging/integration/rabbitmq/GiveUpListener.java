package se.sundsvall.messaging.integration.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Optional.ofNullable;

/**
 * Turns a given-up send request into an outcome, and does nothing else.
 * <p>
 * Everything routed to a channel's dead key is copied to its give-up queue as well as to its parking lot, whether this
 * application put it there or the broker did after unacknowledged redeliveries. That second case is the reason the
 * queue exists: no application code runs on it, so without a consumer here a crash mid-process would emit no outcome at
 * all and postportal would wait on one nobody was going to send.
 * <p>
 * Subclasses carry the {@link org.springframework.amqp.rabbit.annotation.RabbitListener} annotation themselves, because
 * the queue name is a per-channel property placeholder and an inherited annotation could only name one queue.
 *
 * @param <T> this channel's queue payload
 */
public abstract class GiveUpListener<T extends QueueMessage> {

	static final String DEFAULT_REASON = "Delivery was given up on";

	private static final Logger LOG = LoggerFactory.getLogger(GiveUpListener.class);

	private final OutcomePublisher outcomePublisher;

	protected GiveUpListener(final OutcomePublisher outcomePublisher) {
		this.outcomePublisher = outcomePublisher;
	}

	protected final void handle(final T message, final String failureReason) {
		final var reason = ofNullable(failureReason).orElse(DEFAULT_REASON);
		LOG.info("Reporting give-up for recipient {}: {}", message.recipientId(), reason);

		outcomePublisher.publishFailed(message.recipientId(), reason);
	}
}
