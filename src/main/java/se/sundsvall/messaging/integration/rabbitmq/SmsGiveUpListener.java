package se.sundsvall.messaging.integration.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;
import static se.sundsvall.messaging.integration.rabbitmq.SmsRetryPublisher.FAILURE_REASON_HEADER;

/**
 * Turns a given-up request into an outcome, and does nothing else.
 * <p>
 * Everything routed {@code dead} is copied here as well as to the parking lot, whether this application put it there or
 * the broker did after unacknowledged redeliveries. That second case is the reason this queue exists: no application
 * code runs on it, so without a consumer here a crash mid-process would emit no {@code sms.failed} at all and
 * postportal
 * would wait on an outcome nobody was going to send.
 * <p>
 * The queue has its delivery limit disabled on purpose. If the outcome cannot be published, blocking this queue's head
 * is loud, and dropping the message would be silent.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
class SmsGiveUpListener {

	static final String DEFAULT_REASON = "Delivery was given up on";

	private static final Logger LOG = LoggerFactory.getLogger(SmsGiveUpListener.class);

	private final SmsOutcomePublisher outcomePublisher;

	SmsGiveUpListener(final SmsOutcomePublisher outcomePublisher) {
		this.outcomePublisher = outcomePublisher;
	}

	@RabbitListener(queues = "${rabbitmq.give-up-queue}")
	void receive(
		@Payload final SmsQueueMessage message,
		@Header(name = FAILURE_REASON_HEADER, required = false) final String failureReason) {

		final var reason = ofNullable(failureReason).orElse(DEFAULT_REASON);
		LOG.info("Reporting give-up for recipient {}: {}", message.recipientId(), reason);

		outcomePublisher.publishFailed(message.recipientId(), reason);
	}
}
