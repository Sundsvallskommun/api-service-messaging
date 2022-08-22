package se.sundsvall.messaging.integration.smssender;

import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.configuration.RetryProperties;
import se.sundsvall.messaging.dto.SmsDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.processor.Processor;
import se.sundsvall.messaging.service.event.IncomingSmsEvent;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import generated.se.sundsvall.smssender.SendSmsResponse;

@Component
class SmsProcessor extends Processor {

    private final SmsSenderIntegration smsSenderIntegration;
    private final DefaultSettings defaultSettings;

    private final RetryPolicy<ResponseEntity<SendSmsResponse>> retryPolicy;

    SmsProcessor(final RetryProperties retryProperties,
            final MessageRepository messageRepository,
            final HistoryRepository historyRepository,
            final SmsSenderIntegration smsSenderIntegration,
            final DefaultSettings defaultSettings) {
        super(messageRepository, historyRepository);

        this.smsSenderIntegration = smsSenderIntegration;
        this.defaultSettings = defaultSettings;

        retryPolicy = RetryPolicy.<ResponseEntity<SendSmsResponse>>builder()
            .withMaxAttempts(retryProperties.getMaxAttempts())
            .withBackoff(retryProperties.getInitialDelay(), retryProperties.getMaxDelay())
            .handle(Exception.class)
            .handleResultIf(response -> response.getStatusCode() != HttpStatus.OK || !BooleanUtils.isTrue(response.getBody().getSent()))
            .onFailedAttempt(event -> log.info("Unable to send SMS ({}/{}): {}",
                event.getAttemptCount(), retryProperties.getMaxAttempts(), event.getLastException().getMessage()))
            .build();
    }

    @EventListener(IncomingSmsEvent.class)
    void handleIncomingSmsEvent(final IncomingSmsEvent event) {
        var message = messageRepository.findById(event.getMessageId()).orElse(null);

        if (message == null) {
            log.warn("Unable to process missing SMS {}", event.getMessageId());

            return;
        }

        var smsDto = mapToDto(message);

        try {
            Failsafe
                .with(retryPolicy)
                .onSuccess(successEvent -> handleSuccessfulDelivery(message))
                .onFailure(failureEvent -> handleMaximumDeliveryAttemptsExceeded(message))
                .get(() -> smsSenderIntegration.sendSms(smsDto));
        } catch (Exception e) {
            log.warn("Unable to send SMS {}: {}", event.getMessageId(), e.getMessage());
        }
    }

    SmsDto mapToDto(final MessageEntity message) {
        var request = GSON.fromJson(message.getContent(), SmsRequest.class);

        var sender = Optional.ofNullable(request.getSender()).orElseGet(defaultSettings::getSms);

        return SmsDto.builder()
            .withSender(sender)
            .withMobileNumber(request.getMobileNumber())
            .withMessage(request.getMessage())
            .build();
    }
}
