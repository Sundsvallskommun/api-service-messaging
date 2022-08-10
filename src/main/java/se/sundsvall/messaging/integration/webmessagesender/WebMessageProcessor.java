package se.sundsvall.messaging.integration.webmessagesender;

import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.api.model.WebMessageRequest;
import se.sundsvall.messaging.configuration.RetryProperties;
import se.sundsvall.messaging.dto.WebMessageDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.processor.Processor;
import se.sundsvall.messaging.service.event.IncomingWebMessageEvent;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;

@Component
class WebMessageProcessor extends Processor {

    private final WebMessageSenderIntegration webMessageSenderIntegration;

    private final RetryPolicy<ResponseEntity<Void>> retryPolicy;

    WebMessageProcessor(final RetryProperties retryProperties,
            final MessageRepository messageRepository,
            final HistoryRepository historyRepository,
            final WebMessageSenderIntegration webMessageSenderIntegration) {
        super(messageRepository, historyRepository);

        this.webMessageSenderIntegration = webMessageSenderIntegration;

        retryPolicy = RetryPolicy.<ResponseEntity<Void>>builder()
            .withMaxAttempts(retryProperties.getMaxAttempts())
            .withBackoff(retryProperties.getInitialDelay(), retryProperties.getMaxDelay())
            .handle(Exception.class)
            .handleResultIf(response -> response.getStatusCode() != HttpStatus.CREATED)
            .onFailedAttempt(event -> log.debug("Unable to send web message ({}/{}): {}",
                event.getAttemptCount(), retryProperties.getMaxAttempts(), event.getLastException().getMessage()))
            .build();
    }

    @EventListener(IncomingWebMessageEvent.class)
    void handleIncomingWebMessageEvent(final IncomingWebMessageEvent event) {
        var message = messageRepository.findById(event.getMessageId()).orElse(null);

        if (message == null) {
            log.warn("Unable to process missing web message {}", event.getMessageId());

            return;
        }

        var webMessageDto = mapToDto(message);

        try {
            Failsafe
                .with(retryPolicy)
                .onSuccess(successEvent -> handleSuccessfulDelivery(message))
                .onFailure(failureEvent -> handleMaximumDeliveryAttemptsExceeded(message))
                .get(() -> webMessageSenderIntegration.sendWebMessage(webMessageDto));
        } catch (Exception e) {
            log.warn("Unable to send web message {}: {}", event.getMessageId(), e.getMessage());
        }
    }

    WebMessageDto mapToDto(final MessageEntity message) {
        var request = GSON.fromJson(message.getContent(), WebMessageRequest.class);

        return WebMessageDto.builder()
            .withParty(request.getParty())
            .withMessage(request.getMessage())
            .build();
    }
}