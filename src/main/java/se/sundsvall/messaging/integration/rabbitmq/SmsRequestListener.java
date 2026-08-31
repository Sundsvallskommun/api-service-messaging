package se.sundsvall.messaging.integration.rabbitmq;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.messaging.integration.rabbitmq.SmsRetryPublisher.MessageIds;
import se.sundsvall.messaging.service.MessageService;

import static java.util.Optional.ofNullable;
import static se.sundsvall.messaging.integration.rabbitmq.RabbitMapper.toSmsRequest;
import static se.sundsvall.messaging.integration.rabbitmq.SmsRetryPublisher.ATTEMPT_HEADER;
import static se.sundsvall.messaging.integration.rabbitmq.SmsRetryPublisher.BATCH_ID_HEADER;
import static se.sundsvall.messaging.integration.rabbitmq.SmsRetryPublisher.MESSAGE_ID_HEADER;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

/**
 * Consumes SMS send requests from the work queue and drives the retry ladder.
 * <p>
 * The request goes through the ordinary {@link MessageService} path, so a queued SMS is persisted, archived to history
 * and counted in statistics exactly like one submitted over HTTP - and produces the message id that is reported back as
 * the outcome's external id.
 * <p>
 * The message and batch ids are minted here rather than inside the service, so that every attempt at one SMS can be
 * given the same pair. History then shows one message that was tried several times, each attempt its own delivery,
 * instead of several unrelated messages. Minting them up front is also what makes them known on a failed attempt, where
 * no result comes back to read them from.
 * <p>
 * Every branch ends in an ack, because the message is always republished somewhere first: onto a wait queue, or onto
 * {@code dead}. What is deliberately not caught is a failure to publish - that leaves the message unacked, and after
 * the delivery limit the broker dead-letters it onto {@code dead} itself, where the give-up queue still turns it into
 * an outcome.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
class SmsRequestListener {

	private static final Logger LOG = LoggerFactory.getLogger(SmsRequestListener.class);

	private final MessageService messageService;
	private final SmsOutcomePublisher outcomePublisher;
	private final SmsRetryPublisher retryPublisher;

	SmsRequestListener(final MessageService messageService, final SmsOutcomePublisher outcomePublisher, final SmsRetryPublisher retryPublisher) {
		this.messageService = messageService;
		this.outcomePublisher = outcomePublisher;
		this.retryPublisher = retryPublisher;
	}

	@RabbitListener(queues = "${rabbitmq.work-queue}")
	void receive(
		@Payload final SmsQueueMessage message,
		@Header(name = ATTEMPT_HEADER, required = false) final Integer attemptHeader,
		@Header(name = MESSAGE_ID_HEADER, required = false) final String messageIdHeader,
		@Header(name = BATCH_ID_HEADER, required = false) final String batchIdHeader) {

		final var attempt = ofNullable(attemptHeader).orElse(1);
		final var messageIds = new MessageIds(
			ofNullable(messageIdHeader).orElseGet(() -> UUID.randomUUID().toString()),
			ofNullable(batchIdHeader).orElseGet(() -> UUID.randomUUID().toString()));

		LOG.info("Handling queued SMS for recipient {}, attempt {}, message {}", message.recipientId(), attempt, messageIds.messageId());

		try {
			final var result = messageService.sendSms(toSmsRequest(message), messageIds.batchId(), messageIds.messageId());

			if (SENT == result.status()) {
				outcomePublisher.publishSent(message.recipientId(), result.messageId());
				return;
			}

			// A 2xx with sent=false means the gateway took the call and declined it. Treated as transient: a genuinely
			// bad number merely exhausts the ladder a few minutes later, while an operator blip is worth another go.
			handleFailure(message, attempt, "sms-sender reported the message as not sent", messageIds);
		} catch (final ThrowableProblem e) {
			if (e.getStatusCode().is4xxClientError()) {
				// The request itself is wrong, so no amount of waiting will fix it.
				retryPublisher.publishGiveUp(message, "sms-sender rejected the request: " + e.getMessage());
				return;
			}
			handleFailure(message, attempt, e.getMessage(), messageIds);
		}
	}

	private void handleFailure(final SmsQueueMessage message, final int attempt, final String reason, final MessageIds messageIds) {
		final var nextAttempt = attempt + 1;

		if (retryPublisher.hasTierFor(nextAttempt)) {
			retryPublisher.publishRetry(message, nextAttempt, reason, messageIds);
		} else {
			retryPublisher.publishGiveUp(message, "attempts exhausted after %d tries, last failure: %s".formatted(attempt, reason));
		}
	}
}
