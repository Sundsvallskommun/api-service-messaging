package se.sundsvall.messaging.processor;

import static se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod.NO_CONTACT;

import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.api.model.MessageRequest;
import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.feedbacksettings.FeedbackSettingsIntegration;
import se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Sender;
import se.sundsvall.messaging.service.event.IncomingEmailEvent;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.service.event.IncomingSmsEvent;

@Component
class MessageProcessor extends Processor {

    private static final Gson GSON = new GsonBuilder().create();

    private final ApplicationEventPublisher eventPublisher;
    private final DefaultSettings defaultSettings;
    private final FeedbackSettingsIntegration feedbackSettingsIntegration;

    MessageProcessor(final ApplicationEventPublisher eventPublisher,
            final MessageRepository messageRepository,
            final HistoryRepository historyRepository,
            final DefaultSettings defaultSettings,
            final FeedbackSettingsIntegration feedbackSettingsIntegration) {
        super(messageRepository, historyRepository);

        this.eventPublisher = eventPublisher;
        this.defaultSettings = defaultSettings;
        this.feedbackSettingsIntegration = feedbackSettingsIntegration;
    }

    @EventListener(IncomingMessageEvent.class)
    void handleIncomingMessageEvent(final IncomingMessageEvent event) {
        var message = messageRepository.findById(event.getMessageId()).orElse(null);

        if (message == null) {
            return;
        }

        var partyId = message.getPartyId();

        var feedbackChannels = feedbackSettingsIntegration.getSettingsByPartyId(partyId);
        if (feedbackChannels.isEmpty()) {
            log.info("No feedback settings found for {}", partyId);

            historyRepository.save(mapToHistoryEntity(message.withStatus(MessageStatus.NO_FEEDBACK_SETTINGS_FOUND)));
            messageRepository.deleteById(message.getMessageId());
        } else {
            for (var feedbackChannel : feedbackChannels) {
                var actualContactMethod = Optional.ofNullable(feedbackChannel.getContactMethod())
                    .map(contactMethod -> {
                        if (!feedbackChannel.isFeedbackWanted()) {
                            return NO_CONTACT;
                        }

                        return contactMethod;
                    })
                    .orElse(ContactMethod.UNKNOWN);

                switch (actualContactMethod) {
                    case EMAIL -> {
                        log.info("Handling incoming message {} as e-mail", message.getMessageId());

                        messageRepository.save(message
                            .withType(MessageType.EMAIL)
                            .withContent(mapToEmailRequest(message, feedbackChannel.getDestination())));
                        eventPublisher.publishEvent(new IncomingEmailEvent(this, message.getMessageId()));
                    }
                    case SMS -> {
                        log.info("Handling incoming message {} as SMS", message.getMessageId());

                        messageRepository.save(message
                            .withType(MessageType.SMS)
                            .withContent(mapToSmsRequest(message, feedbackChannel.getDestination())));
                        eventPublisher.publishEvent(new IncomingSmsEvent(this, message.getMessageId()));
                    }
                    case NO_CONTACT -> {
                        log.info("No feedback wanted for {}. No delivery will be attempted", partyId);

                        historyRepository.save(mapToHistoryEntity(message.withStatus(MessageStatus.NO_FEEDBACK_WANTED)));
                        messageRepository.deleteById(message.getMessageId());
                    }
                    default -> {
                        log.warn("Unknown/missing contact method for message {}. Will not be delivered",
                                message.getMessageId());

                        historyRepository.save(mapToHistoryEntity(message.withStatus(MessageStatus.FAILED)));
                        messageRepository.deleteById(message.getMessageId());
                    }
                }
            }
        }
    }

    private String mapToEmailRequest(final MessageEntity messageEntity, final String emailAddress) {
        var message = GSON.fromJson(messageEntity.getContent(), MessageRequest.Message.class);

        var sender = Optional.ofNullable(message.getSender())
            .map(Sender::getEmail)
            .orElse(defaultSettings.getEmail());

        var emailRequest = EmailRequest.builder()
            .withParty(message.getParty())
            .withHeaders(message.getHeaders())
            .withSender(sender)
            .withEmailAddress(emailAddress)
            .withSubject(message.getSubject())
            .withMessage(message.getMessage())
            .build();

        return GSON.toJson(emailRequest);
    }

    private String mapToSmsRequest(final MessageEntity messageEntity, final String mobileNumber) {
        var message = GSON.fromJson(messageEntity.getContent(), MessageRequest.Message.class);

        var sender = Optional.ofNullable(message.getSender())
            .map(Sender::getSms)
            .orElse(defaultSettings.getSms());

        var smsRequest = SmsRequest.builder()
            .withParty(message.getParty())
            .withHeaders(message.getHeaders())
            .withSender(sender)
            .withMobileNumber(mobileNumber)
            .withMessage(message.getMessage())
            .build();

        return GSON.toJson(smsRequest);
    }
}
