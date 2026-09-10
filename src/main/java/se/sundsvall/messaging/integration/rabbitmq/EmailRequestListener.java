package se.sundsvall.messaging.integration.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageService;

import static se.sundsvall.messaging.integration.rabbitmq.RabbitMapper.toEmailRequest;

/**
 * Consumes e-mail send requests. Behaviour lives in {@link RequestListener}.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
class EmailRequestListener extends RequestListener<EmailQueueMessage> {

	private final MessageService messageService;

	EmailRequestListener(final MessageService messageService, final EmailOutcomePublisher outcomePublisher, final EmailRetryPublisher retryPublisher) {
		super(outcomePublisher, retryPublisher, "e-mail", "email-sender");
		this.messageService = messageService;
	}

	@RabbitListener(queues = "${rabbitmq.email.work-queue}")
	void receive(
		@Payload final EmailQueueMessage message,
		@Header(name = RetryHeaders.ATTEMPT, required = false) final Integer attemptHeader,
		@Header(name = RetryHeaders.MESSAGE_ID, required = false) final String messageIdHeader,
		@Header(name = RetryHeaders.BATCH_ID, required = false) final String batchIdHeader) {

		handle(message, attemptHeader, messageIdHeader, batchIdHeader);
	}

	@Override
	protected InternalDeliveryResult send(final EmailQueueMessage message, final MessageIds messageIds) {
		return messageService.sendEmail(toEmailRequest(message), messageIds.batchId(), messageIds.messageId());
	}
}
