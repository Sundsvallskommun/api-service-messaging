package se.sundsvall.messaging.integration.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageService;

import static se.sundsvall.messaging.integration.rabbitmq.RabbitMapper.toSmsRequest;

/**
 * Consumes SMS send requests. Behaviour lives in {@link RequestListener}.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
class SmsRequestListener extends RequestListener<SmsQueueMessage> {

	private final MessageService messageService;

	SmsRequestListener(final MessageService messageService, final SmsOutcomePublisher outcomePublisher, final SmsRetryPublisher retryPublisher) {
		super(outcomePublisher, retryPublisher, "SMS", "sms-sender");
		this.messageService = messageService;
	}

	@RabbitListener(queues = "${rabbitmq.sms.work-queue}")
	void receive(
		@Payload final SmsQueueMessage message,
		@Header(name = RetryHeaders.ATTEMPT, required = false) final Integer attemptHeader,
		@Header(name = RetryHeaders.MESSAGE_ID, required = false) final String messageIdHeader,
		@Header(name = RetryHeaders.BATCH_ID, required = false) final String batchIdHeader) {

		handle(message, attemptHeader, messageIdHeader, batchIdHeader);
	}

	@Override
	protected InternalDeliveryResult send(final SmsQueueMessage message, final MessageIds messageIds) {
		return messageService.sendSms(toSmsRequest(message), messageIds.batchId(), messageIds.messageId());
	}
}
