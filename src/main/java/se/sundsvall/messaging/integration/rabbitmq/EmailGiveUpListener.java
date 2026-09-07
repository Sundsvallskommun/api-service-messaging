package se.sundsvall.messaging.integration.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

/**
 * Turns a given-up e-mail request into an outcome, and does nothing else.
 * <p>
 * Everything routed to this channel's dead key is copied here as well as to the parking lot, whether this application
 * put it there or the broker did after unacknowledged redeliveries. That second case is the reason this queue exists:
 * no application code runs on it, so without a consumer here a crash mid-process would emit no outcome at all and
 * postportal would wait on one nobody was going to send.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
class EmailGiveUpListener {

	static final String DEFAULT_REASON = "Delivery was given up on";

	private static final Logger LOG = LoggerFactory.getLogger(EmailGiveUpListener.class);

	private final EmailOutcomePublisher outcomePublisher;

	EmailGiveUpListener(final EmailOutcomePublisher outcomePublisher) {
		this.outcomePublisher = outcomePublisher;
	}

	@RabbitListener(queues = "${rabbitmq.email.give-up-queue}")
	void receive(
		@Payload final EmailQueueMessage message,
		@Header(name = RetryHeaders.FAILURE_REASON, required = false) final String failureReason) {

		final var reason = ofNullable(failureReason).orElse(DEFAULT_REASON);
		LOG.info("Reporting give-up for recipient {}: {}", message.recipientId(), reason);

		outcomePublisher.publishFailed(message.recipientId(), reason);
	}
}
