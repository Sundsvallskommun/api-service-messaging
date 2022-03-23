package se.sundsvall.messaging.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.integration.feedbacksettings.ContactMethod;
import se.sundsvall.messaging.integration.feedbacksettings.FeedbackSettingDto;
import se.sundsvall.messaging.integration.feedbacksettings.FeedbackSettingsIntegration;
import se.sundsvall.messaging.mapper.MessageMapper;
import se.sundsvall.messaging.mapper.UndeliverableMapper;
import se.sundsvall.messaging.model.dto.MessageBatchDto;
import se.sundsvall.messaging.model.dto.UndeliverableMessageDto;
import se.sundsvall.messaging.model.entity.EmailEntity;
import se.sundsvall.messaging.model.entity.MessageEntity;
import se.sundsvall.messaging.model.entity.SmsEntity;
import se.sundsvall.messaging.repository.EmailRepository;
import se.sundsvall.messaging.repository.MessageRepository;
import se.sundsvall.messaging.repository.SmsRepository;

@Service
public class MessageService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final FeedbackSettingsIntegration feedbackSettings;
    private final SmsRepository smsRepository;
    private final EmailRepository emailRepository;
    private final HistoryService historyService;
    private final DefaultSettings defaultSettings;

    public MessageService(MessageRepository messageRepository,
                          FeedbackSettingsIntegration feedbackSettings,
                          SmsRepository smsRepository,
                          EmailRepository emailRepository,
                          HistoryService historyService,
                          DefaultSettings defaultSettings) {
        this.messageRepository = messageRepository;
        this.feedbackSettings = feedbackSettings;
        this.smsRepository = smsRepository;
        this.emailRepository = emailRepository;
        this.historyService = historyService;
        this.defaultSettings = defaultSettings;
    }

    public MessageBatchDto saveIncomingMessages(MessageBatchDto messageBatch) {
        final List<MessageBatchDto.Message> incomingMessages = messageBatch.getMessages();
        final List<MessageEntity> handledMessages = new ArrayList<>();

        for (MessageBatchDto.Message message : incomingMessages) {
            String partyId = message.getPartyId() == null ? "" : message.getPartyId();

            if (partyId.isBlank()) {
                continue;
            }

            MessageEntity incomingMessage = MessageMapper.toEntity(message, messageBatch.getBatchId());
            MessageEntity savedMessage = messageRepository.save(incomingMessage);

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
        List<FeedbackSettingDto.Channel> channels = feedback.getChannels();

        for (FeedbackSettingDto.Channel channel : channels) {
            ContactMethod contactMethod = Optional.ofNullable(channel.getContactMethod())
                    .map(contact -> {
                        if (!channel.isFeedbackWanted()) {
                            return ContactMethod.NO_CONTACT;
                        }

                        return contact;
                    })
                    .orElse(ContactMethod.UNKNOWN);

            switch (contactMethod) {
                case EMAIL:
                    LOG.info("Moving incoming message {} to E-mail", incomingMessage.getMessageId());

                    EmailEntity email = toEmailFromMessageFeedback(incomingMessage, channel);

                    emailRepository.save(email);
                    break;
                case SMS:
                    LOG.info("Moving incoming message {} to SMS", incomingMessage.getMessageId());

                    SmsEntity sms = toSmsFromMessageFeedback(incomingMessage, channel);

                    smsRepository.save(sms);
                    break;
                case NO_CONTACT:
                    LOG.info("No feedback wanted for party {}, message will not be delivered",
                            incomingMessage.getPartyId());

                    UndeliverableMessageDto undeliverable = UndeliverableMapper
                            .toUndeliverable(incomingMessage)
                            .toBuilder()
                            .withStatus(MessageStatus.NO_FEEDBACK_WANTED)
                            .build();

                    historyService.createHistory(undeliverable);
                    break;
                default:
                    LOG.warn("Unknown contact method for message {}, will not be delivered",
                            incomingMessage.getMessageId());

                    UndeliverableMessageDto undeliverableMessage = UndeliverableMapper.toUndeliverable(incomingMessage);

                    historyService.createHistory(undeliverableMessage);
            }
        }

        messageRepository.deleteById(incomingMessage.getMessageId());
    }

    private EmailEntity toEmailFromMessageFeedback(MessageEntity message, FeedbackSettingDto.Channel channel) {
        String senderEmail = StringUtils.isBlank(message.getSenderEmail())
                ? defaultSettings.getEmailAddress()
                : message.getSenderEmail();

        String senderName = StringUtils.isBlank(message.getEmailName())
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
        String sender = StringUtils.isBlank(message.getSmsName())
                ? defaultSettings.getSmsName()
                : message.getSmsName();

        String mobileNumber = channel.getDestination();

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
