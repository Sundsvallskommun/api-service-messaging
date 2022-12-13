package se.sundsvall.messaging.integration.smssender;

import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.configuration.Defaults;
import se.sundsvall.messaging.configuration.RetryProperties;
import se.sundsvall.messaging.dto.SmsDto;
import se.sundsvall.messaging.integration.db.CounterRepository;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.processor.Processor;
import se.sundsvall.messaging.service.WhitelistingService;
import se.sundsvall.messaging.service.event.IncomingSmsEvent;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import generated.se.sundsvall.smssender.SendSmsResponse;

@Component
class SmsProcessor extends Processor {

    private final SmsSenderIntegration smsSenderIntegration;
    private final Defaults defaults;

    private final RetryPolicy<ResponseEntity<SendSmsResponse>> retryPolicy;

    SmsProcessor(final RetryProperties retryProperties,
            final MessageRepository messageRepository,
            final HistoryRepository historyRepository,
            final CounterRepository counterRepository,
            final WhitelistingService whitelistingService,
            final SmsSenderIntegration smsSenderIntegration,
            final Defaults defaults) {
        super(messageRepository, historyRepository, counterRepository, whitelistingService);

        this.smsSenderIntegration = smsSenderIntegration;
        this.defaults = defaults;

        retryPolicy = RetryPolicy.<ResponseEntity<SendSmsResponse>>builder()
            .withMaxAttempts(retryProperties.getMaxAttempts())
            .withBackoff(retryProperties.getInitialDelay(), retryProperties.getMaxDelay())
            .handle(Exception.class)
            .handleResultIf(response -> response.getStatusCode() != HttpStatus.OK || !BooleanUtils.isTrue(response.getBody().getSent()))
            .onFailedAttempt(event -> log.info("Unable to send SMS ({}/{}): {}",
                event.getAttemptCount(), retryProperties.getMaxAttempts(), event.getLastException().getMessage()))
            .build();
    }

    @Transactional
    @EventListener(IncomingSmsEvent.class)
    void handleIncomingSmsEvent(final IncomingSmsEvent event) {
        var message = messageRepository.findByDeliveryId(event.getPayload()).orElse(null);

        if (message == null) {
            log.warn("Unable to process missing SMS {}", event.getPayload());

            return;
        }

        // Register a delivery attempt
        incrementAttemptCounter(MessageType.SMS);

        var smsDto = mapToDto(message);

        // Check if the recipient is whitelisted
        if (!isWhitelisted(message, smsDto.getMobileNumber())) {
            return;
        }

        try {
            Failsafe
                .with(retryPolicy)
                .onSuccess(successEvent -> handleSuccessfulDelivery(message))
                .onFailure(failureEvent -> handleMaximumDeliveryAttemptsExceeded(message))
                .get(() -> smsSenderIntegration.sendSms(smsDto));
        } catch (Exception e) {
            log.warn("Unable to send SMS {}: {}", event.getPayload(), e.getMessage());
        }
    }

    SmsDto mapToDto(final MessageEntity message) {
        var request = GSON.fromJson(message.getContent(), SmsRequest.class);

        // Use sender from the original request, or use default sender as fallback
        var sender = Optional.ofNullable(request.getSender()).orElseGet(defaults::getSms);

        return SmsDto.builder()
            .withSender(sender)
            .withMobileNumber(request.getMobileNumber())
            .withMessage(request.getMessage())
            .build();
    }
}
