package se.sundsvall.messaging.integration.webmessagesender;

import java.util.List;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    @EventListener(IncomingWebMessageEvent.class)
    void handleIncomingWebMessageEvent(final IncomingWebMessageEvent event) {
        var message = messageRepository.findByDeliveryId(event.getPayload()).orElse(null);

        if (message == null) {
            log.warn("Unable to process missing web message {}", event.getPayload());

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
            log.warn("Unable to send web message {}: {}", event.getPayload(), e.getMessage());
        }
    }

    WebMessageDto mapToDto(final MessageEntity message) {
        var request = GSON.fromJson(message.getContent(), WebMessageRequest.class);

        return WebMessageDto.builder()
            .withParty(request.getParty())
            .withMessage(request.getMessage())
            .withAttachments(Optional.ofNullable(request.getAttachments()).orElse(List.of()).stream()
                .map(attachment -> WebMessageDto.AttachmentDto.builder()
                    .withFileName(attachment.getFileName())
                    .withMimeType(attachment.getMimeType())
                    .withBase64Data(attachment.getBase64Data())
                    .build())
                .toList())
            .build();
    }
}
