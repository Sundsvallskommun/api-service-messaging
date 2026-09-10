package se.sundsvall.messaging.integration.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Reports SMS requests that were given up on. Behaviour lives in {@link GiveUpListener}.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
class SmsGiveUpListener extends GiveUpListener<SmsQueueMessage> {

	SmsGiveUpListener(final SmsOutcomePublisher outcomePublisher) {
		super(outcomePublisher);
	}

	@RabbitListener(queues = "${rabbitmq.sms.give-up-queue}")
	void receive(
		@Payload final SmsQueueMessage message,
		@Header(name = RetryHeaders.FAILURE_REASON, required = false) final String failureReason) {

		handle(message, failureReason);
	}
}
