package se.sundsvall.messaging.integration.rabbitmq;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageService;

import static java.util.Optional.ofNullable;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

/**
 * Consumes send requests from a channel's work queue and drives its retry ladder.
 * <p>
 * The request goes through the ordinary {@link MessageService} path, so a queued message is persisted, archived to
 * history and counted in statistics exactly like one submitted over HTTP - and any attachment references it carries are
 * resolved where every other delivery resolves them, inside the delivery attempt, so the object store is covered by
 * this ladder rather than sitting outside it.
 * <p>
 * The message and batch ids are minted here rather than inside the service, so that every attempt at one request can be
 * given the same pair. History then shows one message that was tried several times, each attempt its own delivery,
 * instead of several unrelated messages.
 * <p>
 * Every branch ends in an ack, because the message is always republished somewhere first: onto a wait queue, or onto
 * the dead key. What is deliberately not caught is a failure to publish - that leaves the message unacked, and after
 * the delivery limit the broker dead-letters it itself, where the give-up queue still turns it into an outcome.
 * <p>
 * Subclasses carry the {@link org.springframework.amqp.rabbit.annotation.RabbitListener} annotation themselves, because
 * the queue name is a per-channel property placeholder and an inherited annotation could only name one queue.
 *
 * @param <T> this channel's queue payload
 */
public abstract class RequestListener<T extends QueueMessage> {

	private static final Logger LOG = LoggerFactory.getLogger(RequestListener.class);

	private final OutcomePublisher outcomePublisher;
	private final RetryPublisher<T> retryPublisher;
	private final String channel;
	private final String sender;

	/**
	 * @param channel the channel's name as it should read in a log line, e.g. {@code "e-mail"}
	 * @param sender  the downstream service named in a failure reason, e.g. {@code "email-sender"}
	 */
	protected RequestListener(final OutcomePublisher outcomePublisher, final RetryPublisher<T> retryPublisher,
		final String channel, final String sender) {
		this.outcomePublisher = outcomePublisher;
		this.retryPublisher = retryPublisher;
		this.channel = channel;
		this.sender = sender;
	}

	/**
	 * One delivery attempt, on the same service method the REST endpoint calls.
	 */
	protected abstract InternalDeliveryResult send(T message, MessageIds messageIds);

	/**
	 * The batch id a first attempt runs under, when no retry header has carried one in yet.
	 * <p>
	 * A random one is right for every channel where the batch is this service's own bookkeeping. Snail mail overrides
	 * it, because there the batch id is also what snailmail-sender groups a letter's recipients by, and postportal has
	 * already decided what that grouping is.
	 */
	protected String initialBatchId(final T message) {
		return UUID.randomUUID().toString();
	}

	protected final void handle(final T message, final Integer attemptHeader, final String messageIdHeader, final String batchIdHeader) {
		final var attempt = ofNullable(attemptHeader).orElse(1);
		final var messageIds = new MessageIds(
			ofNullable(messageIdHeader).orElseGet(() -> UUID.randomUUID().toString()),
			ofNullable(batchIdHeader).orElseGet(() -> initialBatchId(message)));

		LOG.info("Handling queued {} for recipient {}, attempt {}, message {}", channel, message.recipientId(), attempt, messageIds.messageId());

		try {
			final var result = send(message, messageIds);

			if (SENT == result.status()) {
				outcomePublisher.publishSent(message.recipientId(), result.messageId());
				return;
			}

			// A 2xx that does not say SENT means the sender took the call and declined it. Treated as transient: a
			// genuinely bad recipient merely exhausts the ladder a few minutes later, while a downstream blip is worth
			// another go.
			handleFailure(message, attempt, sender + " reported the message as not sent", messageIds);
		} catch (final ThrowableProblem e) {
			// Permanence is decided by the problem's type, not its status. dept44's error decoder rewrites the status of
			// every response it is not told to bypass to BAD_GATEWAY, so getStatusCode() reads 502 for a 400 from the
			// sender just as it does for a 500 - but the decoder still picks ClientProblem for a 4xx and ServerProblem
			// for a 5xx, so the type survives the rewrite and is the only thing that can be trusted here. A missing or
			// expired attachment object arrives the same way, and is equally permanent: the ladder cannot bring an
			// expired object back, it can only spend five minutes finding that out.
			if (e instanceof ClientProblem) {
				retryPublisher.publishGiveUp(message, sender + " rejected the request: " + e.getMessage());
				return;
			}
			handleFailure(message, attempt, e.getMessage(), messageIds);
		}
	}

	private void handleFailure(final T message, final int attempt, final String reason, final MessageIds messageIds) {
		final var nextAttempt = attempt + 1;

		if (retryPublisher.hasTierFor(nextAttempt)) {
			retryPublisher.publishRetry(message, nextAttempt, reason, messageIds);
			return;
		}

		retryPublisher.publishGiveUp(message, "attempts exhausted after %d tries, last failure: %s".formatted(attempt, reason));
	}
}
