package se.sundsvall.messaging.service;

import java.util.ArrayList;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.dto.MessageBatchDto;
import se.sundsvall.messaging.integration.db.EmailRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.SmsRepository;
import se.sundsvall.messaging.integration.db.entity.EmailEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.db.entity.SmsEntity;
import se.sundsvall.messaging.integration.feedbacksettings.FeedbackSettingsIntegration;
import se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod;
import se.sundsvall.messaging.integration.feedbacksettings.model.FeedbackSettingDto;
import se.sundsvall.messaging.mapper.MessageMapper;
import se.sundsvall.messaging.mapper.UndeliverableMapper;
import se.sundsvall.messaging.model.MessageStatus;

@Service
public class MessageService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final SmsRepository smsRepository;
    private final EmailRepository emailRepository;
    private final HistoryService historyService;
    private final DefaultSettings defaultSettings;
    private final FeedbackSettingsIntegration feedbackSettings;

    public MessageService(final MessageRepository messageRepository, final SmsRepository smsRepository,
            final EmailRepository emailRepository, final HistoryService historyService,
            final DefaultSettings defaultSettings, final FeedbackSettingsIntegration feedbackSettings) {
        this.messageRepository = messageRepository;
        this.smsRepository = smsRepository;
        this.emailRepository = emailRepository;
        this.historyService = historyService;
        this.defaultSettings = defaultSettings;
        this.feedbackSettings = feedbackSettings;
    }

    public MessageBatchDto saveIncomingMessages(MessageBatchDto messageBatch) {
        var incomingMessages = messageBatch.getMessages();
        var handledMessages = new ArrayList<MessageEntity>();

        for (MessageBatchDto.Message message : incomingMessages) {
            var partyId = message.getParty().getPartyId() == null ? "" : message.getParty().getPartyId();

            if (partyId.isBlank()) {
                continue;
            }

            var incomingMessage = MessageMapper.toEntity(message, messageBatch.getBatchId());
            var savedMessage = messageRepository.save(incomingMessage);

            var listOfFeedbackSettings = feedbackSettings.getSettingsByPartyId(partyId);

            if (listOfFeedbackSettings.isEmpty()) {
                var undeliverable = UndeliverableMapper.toUndeliverable(savedMessage)
                    .toBuilder()
                    .withStatus(MessageStatus.NO_FEEDBACK_SETTINGS_FOUND)
                    .build();

                historyService.createHistory(undeliverable);
                messageRepository.deleteById(savedMessage.getMessageId());
                continue;
            }

            for (FeedbackSettingDto feedback : listOfFeedbackSettings) {
                moveIncomingMessage(savedMessage, feedback);
                handledMessages.add(savedMessage);
            }

        }
        return MessageMapper.toMessageBatch(handledMessages);
    }

    void moveIncomingMessage(MessageEntity incomingMessage, FeedbackSettingDto feedback) {
        var channels = feedback.getChannels();

        for (FeedbackSettingDto.Channel channel : channels) {
            var contactMethod = Optional.ofNullable(channel.getContactMethod())
                .map(contact -> {
                    if (!channel.isFeedbackWanted()) {
                        return ContactMethod.NO_CONTACT;
                    }

                    return contact;
                })
                .orElse(ContactMethod.UNKNOWN);

            switch (contactMethod) {
                case EMAIL:
                    LOG.info("Moving incoming message {} to e-mail", incomingMessage.getMessageId());

                    var email = toEmailFromMessageFeedback(incomingMessage, channel);

                    emailRepository.save(email);
                    break;
                case SMS:
                    LOG.info("Moving incoming message {} to SMS", incomingMessage.getMessageId());

                    var sms = toSmsFromMessageFeedback(incomingMessage, channel);

                    smsRepository.save(sms);
                    break;
                case NO_CONTACT:
                    LOG.info("No feedback wanted for party {}, message will not be delivered",
                            incomingMessage.getPartyId());

                    var undeliverable = UndeliverableMapper
                        .toUndeliverable(incomingMessage)
                        .toBuilder()
                        .withStatus(MessageStatus.NO_FEEDBACK_WANTED)
                        .build();

                    historyService.createHistory(undeliverable);
                    break;
                default:
                    LOG.warn("Unknown contact method for message {}, will not be delivered",
                            incomingMessage.getMessageId());

                    var undeliverableMessage = UndeliverableMapper.toUndeliverable(incomingMessage);
                    historyService.createHistory(undeliverableMessage);
            }
        }

        messageRepository.deleteById(incomingMessage.getMessageId());
    }

    private EmailEntity toEmailFromMessageFeedback(MessageEntity message, FeedbackSettingDto.Channel channel) {
        var senderEmail = StringUtils.isBlank(message.getSenderEmail())
                ? defaultSettings.getEmailAddress()
                : message.getSenderEmail();

        var senderName = StringUtils.isBlank(message.getEmailName())
                ? defaultSettings.getEmailName()
                : message.getEmailName();

        return EmailEntity.builder()
            .withBatchId(message.getBatchId())
            .withMessageId(message.getMessageId())
            .withPartyId(message.getPartyId())
            .withSubject(message.getSubject())
            .withMessage(message.getMessage())
            .withStatus(MessageStatus.PENDING)
            .withEmailAddress(channel.getDestination())
            .withSenderName(senderName)
            .withSenderEmail(senderEmail)
            .build();
    }

    private SmsEntity toSmsFromMessageFeedback(MessageEntity message, FeedbackSettingDto.Channel channel) {
        var sender = StringUtils.isBlank(message.getSmsName())
                ? defaultSettings.getSmsName()
                : message.getSmsName();

        var mobileNumber = channel.getDestination();

        if (mobileNumber.startsWith("07")) {
            mobileNumber = "+467" + mobileNumber.substring(2);
        }

        return SmsEntity.builder()
            .withBatchId(message.getBatchId())
            .withMessageId(message.getMessageId())
            .withMessage(message.getMessage())
            .withStatus(MessageStatus.PENDING)
            .withPartyId(message.getPartyId())
            .withSender(sender)
            .withMobileNumber(mobileNumber)
            .build();
    }
}
