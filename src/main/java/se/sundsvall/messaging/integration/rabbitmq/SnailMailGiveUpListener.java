package se.sundsvall.messaging.integration.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Reports snail mail requests that were given up on. Behaviour lives in {@link GiveUpListener}.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
class SnailMailGiveUpListener extends GiveUpListener<SnailMailQueueMessage> {

	SnailMailGiveUpListener(final SnailMailOutcomePublisher outcomePublisher) {
		super(outcomePublisher);
	}

	@RabbitListener(queues = "${rabbitmq.snail-mail.give-up-queue}")
	void receive(
		@Payload final SnailMailQueueMessage message,
		@Header(name = RetryHeaders.FAILURE_REASON, required = false) final String failureReason) {

		handle(message, failureReason);
	}
}
