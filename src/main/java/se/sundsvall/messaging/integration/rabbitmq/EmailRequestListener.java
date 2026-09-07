package se.sundsvall.messaging.integration.rabbitmq;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.messaging.service.MessageService;

import static java.util.Optional.ofNullable;
import static se.sundsvall.messaging.integration.rabbitmq.RabbitMapper.toEmailRequest;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

/**
 * Consumes e-mail send requests from the work queue and drives the retry ladder.
 * <p>
 * The request goes through the ordinary {@link MessageService} path, so a queued e-mail is persisted, archived to
 * history and counted in statistics exactly like one submitted over HTTP - and any attachment references it carries are
 * resolved where every other delivery resolves them, inside the delivery attempt, so the object store is covered by
 * this ladder rather than sitting outside it.
 * <p>
 * The message and batch ids are minted here rather than inside the service, so that every attempt at one e-mail can be
 * given the same pair. History then shows one message that was tried several times, each attempt its own delivery,
 * instead of several unrelated messages.
 * <p>
 * Every branch ends in an ack, because the message is always republished somewhere first: onto a wait queue, or onto
 * the dead key. What is deliberately not caught is a failure to publish - that leaves the message unacked, and after
 * the delivery limit the broker dead-letters it itself, where the give-up queue still turns it into an outcome.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
class EmailRequestListener {

	private static final Logger LOG = LoggerFactory.getLogger(EmailRequestListener.class);

	private final MessageService messageService;
	private final EmailOutcomePublisher outcomePublisher;
	private final EmailRetryPublisher retryPublisher;

	EmailRequestListener(final MessageService messageService, final EmailOutcomePublisher outcomePublisher, final EmailRetryPublisher retryPublisher) {
		this.messageService = messageService;
		this.outcomePublisher = outcomePublisher;
		this.retryPublisher = retryPublisher;
	}

	@RabbitListener(queues = "${rabbitmq.email.work-queue}")
	void receive(
		@Payload final EmailQueueMessage message,
		@Header(name = RetryHeaders.ATTEMPT, required = false) final Integer attemptHeader,
		@Header(name = RetryHeaders.MESSAGE_ID, required = false) final String messageIdHeader,
		@Header(name = RetryHeaders.BATCH_ID, required = false) final String batchIdHeader) {

		final var attempt = ofNullable(attemptHeader).orElse(1);
		final var messageIds = new MessageIds(
			ofNullable(messageIdHeader).orElseGet(() -> UUID.randomUUID().toString()),
			ofNullable(batchIdHeader).orElseGet(() -> UUID.randomUUID().toString()));

		LOG.info("Handling queued e-mail for recipient {}, attempt {}, message {}", message.recipientId(), attempt, messageIds.messageId());

		try {
			final var result = messageService.sendEmail(toEmailRequest(message), messageIds.batchId(), messageIds.messageId());

			if (SENT == result.status()) {
				outcomePublisher.publishSent(message.recipientId(), result.messageId());
				return;
			}

			handleFailure(message, attempt, "email-sender reported the message as not sent", messageIds);
		} catch (final ThrowableProblem e) {
			// Permanence is decided by the problem's type, not its status. dept44's error decoder rewrites the status of
			// every response it is not told to bypass to BAD_GATEWAY, so getStatusCode() reads 502 for a 400 from
			// email-sender just as it does for a 500 - but the decoder still picks ClientProblem for a 4xx and
			// ServerProblem for a 5xx, so the type survives the rewrite and is the only thing that can be trusted here.
			// A missing or expired attachment object arrives the same way, and is equally permanent: the ladder cannot
			// bring an expired object back, it can only spend five minutes finding that out.
			if (e instanceof ClientProblem) {
				retryPublisher.publishGiveUp(message, "email-sender rejected the request: " + e.getMessage());
				return;
			}
			handleFailure(message, attempt, e.getMessage(), messageIds);
		}
	}

	private void handleFailure(final EmailQueueMessage message, final int attempt, final String reason, final MessageIds messageIds) {
		final var nextAttempt = attempt + 1;

		if (retryPublisher.hasTierFor(nextAttempt)) {
			retryPublisher.publishRetry(message, nextAttempt, reason, messageIds);
		} else {
			retryPublisher.publishGiveUp(message, "attempts exhausted after %d tries, last failure: %s".formatted(attempt, reason));
		}
	}
}
