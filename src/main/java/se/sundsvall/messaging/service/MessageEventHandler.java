package se.sundsvall.messaging.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

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

    @TransactionalEventListener(value = IncomingMessageEvent.class, fallbackExecution = true)
    public void handleIncomingMessageEvent(final IncomingMessageEvent event) {
        // Get the message (delivery)
        var message = dbIntegration.getMessageByDeliveryId(event.getDeliveryId())
            .orElseThrow(() -> Problem.valueOf(Status.INTERNAL_SERVER_ERROR,
                "Unable to send " + event.getMessageType() + " with id " + event.getDeliveryId()));
        // Deliver it
        messageService.deliver(message);
    }
}
