package se.sundsvall.messaging.processor;

import static se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod.EMAIL;
import static se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod.NO_CONTACT;
import static se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod.SMS;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.api.model.MessageRequest;
import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.integration.businessrules.BusinessRulesIntegration;
import se.sundsvall.messaging.integration.businessrules.domain.DistributionRuleEvaluationDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.feedbacksettings.FeedbackSettingsIntegration;
import se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod;
import se.sundsvall.messaging.model.Header;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Sender;
import se.sundsvall.messaging.service.event.IncomingEmailEvent;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.service.event.IncomingSmsEvent;

import generated.se.sundsvall.businessrules.HeaderName;

@Component
class MessageProcessor extends Processor {

    private static final Gson GSON = new GsonBuilder().create();

    private final ApplicationEventPublisher eventPublisher;
    private final DefaultSettings defaultSettings;
    private final FeedbackSettingsIntegration feedbackSettingsIntegration;
    private final BusinessRulesIntegration businessRulesIntegration;

    MessageProcessor(final ApplicationEventPublisher eventPublisher,
            final MessageRepository messageRepository,
            final HistoryRepository historyRepository,
            final DefaultSettings defaultSettings,
            final FeedbackSettingsIntegration feedbackSettingsIntegration,
            final BusinessRulesIntegration businessRulesIntegration) {
        super(messageRepository, historyRepository);

        this.eventPublisher = eventPublisher;
        this.defaultSettings = defaultSettings;
        this.feedbackSettingsIntegration = feedbackSettingsIntegration;
        this.businessRulesIntegration = businessRulesIntegration;
    }

    @Transactional
    @EventListener(IncomingMessageEvent.class)
    void handleIncomingMessageEvent(final IncomingMessageEvent event) {
        var message = messageRepository.findById(event.getPayload()).orElse(null);

        if (message == null) {
            return;
        }

        var partyId = message.getPartyId();
        var headerMap = getHeaders(message);

        var feedbackChannels = feedbackSettingsIntegration.getSettingsByPartyId(headerMap.values(), partyId);
        if (feedbackChannels.isEmpty()) {
            log.info("No feedback settings found for {}", partyId);

            historyRepository.save(mapToHistoryEntity(message.withStatus(MessageStatus.NO_FEEDBACK_SETTINGS_FOUND)));
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

                var shouldSend = Optional.of(actualContactMethod)
                    // Filter on contact method being EMAIL or SMS to avoid calling the business-rules
                    // service unless needed
                    .filter(contactMethod -> contactMethod == EMAIL || contactMethod == SMS)
                    // Extract the distribution rule header
                    .map(ignored -> headerMap.get(HeaderName.DISTRIBUTION_RULE))
                    .map(Header::getValues)
                    .map(values -> values.get(0))// TODO: handle if there are multiple distribution rule headers ??
                    .stream().peek(distributionRule ->
                        log.info("Evaluating distribution rule {}", distributionRule)
                    ).findFirst()
                    // Call the business-rules service to evaluate the distribution rule
                    .map(businessRulesIntegration::evaluate)
                    .map(DistributionRuleEvaluationDto::getResult)
                    .map(distributionRuleEvaluationResult -> distributionRuleEvaluationResult.get(actualContactMethod))
                    .orElse(true);

                switch (actualContactMethod) {
                    case EMAIL -> {
                        var deliveryId = UUID.randomUUID().toString();

                        log.info("Handling incoming message {} as e-mail with delivery id {}", message.getMessageId(), deliveryId);

                        var delivery = MessageEntity.builder()
                            .withMessageId(message.getMessageId())
                            .withBatchId(message.getBatchId())
                            .withDeliveryId(deliveryId)
                            .withPartyId(message.getPartyId())
                            .withType(MessageType.EMAIL)
                            .withStatus(message.getStatus())
                            .withContent(mapToEmailRequest(message, feedbackChannel.getDestination()))
                            .withCreatedAt(message.getCreatedAt())
                            .build();

                        messageRepository.save(delivery);

                        eventPublisher.publishEvent(new IncomingEmailEvent(this, deliveryId));
                    }
                    case SMS -> {
                        var deliveryId = UUID.randomUUID().toString();

                        log.info("Handling incoming message {} as SMS with delivery id {}", message.getMessageId(), deliveryId);

                        var delivery = MessageEntity.builder()
                            .withMessageId(message.getMessageId())
                            .withBatchId(message.getBatchId())
                            .withDeliveryId(deliveryId)
                            .withPartyId(message.getPartyId())
                            .withType(MessageType.SMS)
                            .withStatus(message.getStatus())
                            .withContent(mapToSmsRequest(message, feedbackChannel.getDestination()))
                            .withCreatedAt(message.getCreatedAt())
                            .build();

                        messageRepository.save(delivery);

                        eventPublisher.publishEvent(new IncomingSmsEvent(this, deliveryId));
                    }
                    case NO_CONTACT -> {
                        log.info("No feedback wanted for {}. No delivery will be attempted", partyId);

                        historyRepository.save(mapToHistoryEntity(message.withStatus(MessageStatus.NO_FEEDBACK_WANTED)));
                    }
                    default -> {
                        log.warn("Unknown/missing contact method for message {}. Will not be delivered",
                                message.getMessageId());

                        historyRepository.save(mapToHistoryEntity(message.withStatus(MessageStatus.FAILED)));
                    }
                }
            }
        }

        messageRepository.deleteById(message.getId());
    }

    String mapToEmailRequest(final MessageEntity messageEntity, final String emailAddress) {
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
            .withHtmlMessage(message.getHtmlMessage())
            .build();

        return GSON.toJson(emailRequest);
    }

    String mapToSmsRequest(final MessageEntity messageEntity, final String mobileNumber) {
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

    Map<HeaderName, Header> getHeaders(final MessageEntity messageEntity) {
        var message = GSON.fromJson(messageEntity.getContent(), MessageRequest.Message.class);

        return message.getHeaders().stream().collect(Collectors.toMap(Header::getName, Function.identity()));
    }
}
