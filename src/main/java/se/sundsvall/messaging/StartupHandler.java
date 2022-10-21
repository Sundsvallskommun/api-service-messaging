package se.sundsvall.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.service.event.IncomingDigitalMailEvent;
import se.sundsvall.messaging.service.event.IncomingEmailEvent;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.service.event.IncomingSmsEvent;
import se.sundsvall.messaging.service.event.IncomingSnailmailEvent;
import se.sundsvall.messaging.service.event.IncomingWebMessageEvent;

@Component
class StartupHandler implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(StartupHandler.class);

    private final ApplicationEventPublisher eventPublisher;
    private final MessageRepository messageRepository;

    StartupHandler(final ApplicationEventPublisher eventPublisher, final MessageRepository messageRepository) {
        this.eventPublisher = eventPublisher;
        this.messageRepository = messageRepository;
    }

    @Override
    public void run(String... args) {
        messageRepository.findLatestWithStatus(MessageStatus.PENDING).stream()
            .peek(message -> LOG.info("Processing {} with id {}", message.getType(), message.getMessageId()))
            .map(message -> switch (message.getType()) {
                case EMAIL -> new IncomingEmailEvent(this, message.getMessageId());
                case SMS -> new IncomingSmsEvent(this, message.getMessageId());
                case MESSAGE -> new IncomingMessageEvent(this, message.getId());
                case WEB_MESSAGE -> new IncomingWebMessageEvent(this, message.getMessageId());
                case DIGITAL_MAIL -> new IncomingDigitalMailEvent(this, message.getMessageId());
                case SNAIL_MAIL -> new IncomingSnailmailEvent(this, message.getMessageId());
            })
            .forEach(eventPublisher::publishEvent);
    }
}
