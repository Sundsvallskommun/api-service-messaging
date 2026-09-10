package se.sundsvall.messaging.integration.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageService;

import static se.sundsvall.messaging.integration.rabbitmq.RabbitMapper.toDigitalMailRequest;

/**
 * Consumes digital mail send requests. Behaviour lives in {@link RequestListener}.
 * <p>
 * The single-party overload of {@code sendDigitalMail} is the one used here rather than the batch-shaped one the REST
 * endpoint calls: a queued request is already one recipient's worth of work, and asking for one delivery back is what
 * lets an attempt reuse the ids the previous attempt ran under.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
class DigitalMailRequestListener extends RequestListener<DigitalMailQueueMessage> {

	private final MessageService messageService;

	DigitalMailRequestListener(final MessageService messageService, final DigitalMailOutcomePublisher outcomePublisher,
		final DigitalMailRetryPublisher retryPublisher) {
		super(outcomePublisher, retryPublisher, "digital mail", "digital-mail-sender");
		this.messageService = messageService;
	}

	@RabbitListener(queues = "${rabbitmq.digital-mail.work-queue}")
	void receive(
		@Payload final DigitalMailQueueMessage message,
		@Header(name = RetryHeaders.ATTEMPT, required = false) final Integer attemptHeader,
		@Header(name = RetryHeaders.MESSAGE_ID, required = false) final String messageIdHeader,
		@Header(name = RetryHeaders.BATCH_ID, required = false) final String batchIdHeader) {

		handle(message, attemptHeader, messageIdHeader, batchIdHeader);
	}

	@Override
	protected InternalDeliveryResult send(final DigitalMailQueueMessage message, final MessageIds messageIds) {
		return messageService.sendDigitalMail(toDigitalMailRequest(message), message.organizationNumber(),
			messageIds.batchId(), messageIds.messageId());
	}
}
