package se.sundsvall.messaging.integration.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Reports digital mail requests that were given up on. Behaviour lives in {@link GiveUpListener}.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
class DigitalMailGiveUpListener extends GiveUpListener<DigitalMailQueueMessage> {

	DigitalMailGiveUpListener(final DigitalMailOutcomePublisher outcomePublisher) {
		super(outcomePublisher);
	}

	@RabbitListener(queues = "${rabbitmq.digital-mail.give-up-queue}")
	void receive(
		@Payload final DigitalMailQueueMessage message,
		@Header(name = RetryHeaders.FAILURE_REASON, required = false) final String failureReason) {

		handle(message, failureReason);
	}
}
