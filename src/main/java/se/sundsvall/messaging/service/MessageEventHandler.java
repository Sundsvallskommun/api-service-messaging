package se.sundsvall.messaging.service;

import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;

@Component
class MessageEventHandler {

	private final MessageService messageService;

	private final DbIntegration dbIntegration;

	MessageEventHandler(final MessageService messageService, final DbIntegration dbIntegration) {
		this.messageService = messageService;
		this.dbIntegration = dbIntegration;
	}

	@Async
	@TransactionalEventListener(value = IncomingMessageEvent.class, fallbackExecution = true)
	public void handleIncomingMessageEvent(final IncomingMessageEvent event) {
		try {
			RequestId.init();

			// Get the message (delivery)
			final var message = dbIntegration.getMessageByDeliveryId(event.getDeliveryId())
				.orElseThrow(() -> Problem.valueOf(HttpStatus.INTERNAL_SERVER_ERROR,
					"Unable to send " + event.getMessageType() + " with id " + event.getDeliveryId()));

			// Handle it
			if (message.type() == MESSAGE) {
				messageService.sendMessage(message);
			} else if (message.type() == LETTER) {
				messageService.sendLetter(message);
			} else {
				messageService.deliver(message);
			}
		} finally {
			RequestId.reset();
		}
	}

}
