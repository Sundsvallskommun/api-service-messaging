package se.sundsvall.messaging.integration.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageService;

import static se.sundsvall.messaging.integration.rabbitmq.RabbitMapper.toSnailMailRequest;

/**
 * Consumes snail mail send requests. Behaviour lives in {@link RequestListener}.
 * <p>
 * This is the one channel where the batch id is not this service's own bookkeeping: snailmail-sender groups a letter's
 * recipients by it, and its scheduler flushes the group on its own timetable. Postportal has already decided what that
 * grouping is - it sends its message id, exactly as it does over REST - so a first attempt runs under the id the
 * request carries rather than under a fresh one. Minting one here would scatter a letter's recipients across as many
 * batches as it has recipients, and each would be posted separately.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
class SnailMailRequestListener extends RequestListener<SnailMailQueueMessage> {

	private final MessageService messageService;

	SnailMailRequestListener(final MessageService messageService, final SnailMailOutcomePublisher outcomePublisher,
		final SnailMailRetryPublisher retryPublisher) {
		super(outcomePublisher, retryPublisher, "snail mail", "snailmail-sender");
		this.messageService = messageService;
	}

	@RabbitListener(queues = "${rabbitmq.snail-mail.work-queue}")
	void receive(
		@Payload final SnailMailQueueMessage message,
		@Header(name = RetryHeaders.ATTEMPT, required = false) final Integer attemptHeader,
		@Header(name = RetryHeaders.MESSAGE_ID, required = false) final String messageIdHeader,
		@Header(name = RetryHeaders.BATCH_ID, required = false) final String batchIdHeader) {

		handle(message, attemptHeader, messageIdHeader, batchIdHeader);
	}

	@Override
	protected String initialBatchId(final SnailMailQueueMessage message) {
		return message.batchId();
	}

	@Override
	protected InternalDeliveryResult send(final SnailMailQueueMessage message, final MessageIds messageIds) {
		return messageService.sendSnailMail(toSnailMailRequest(message), messageIds.batchId(), messageIds.messageId());
	}
}
