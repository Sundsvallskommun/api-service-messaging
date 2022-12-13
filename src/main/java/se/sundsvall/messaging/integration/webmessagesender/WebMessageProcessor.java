package se.sundsvall.messaging.integration.webmessagesender;

import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.messaging.api.model.WebMessageRequest;
import se.sundsvall.messaging.configuration.RetryProperties;
import se.sundsvall.messaging.dto.WebMessageDto;
import se.sundsvall.messaging.integration.db.CounterRepository;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.MessageType;
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
            final CounterRepository counterRepository,
            final WebMessageSenderIntegration webMessageSenderIntegration) {
        super(messageRepository, historyRepository, counterRepository);

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

        // Register a delivery attempt
        incrementAttemptCounter(MessageType.WEB_MESSAGE);

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

        var attachments = Optional.ofNullable(request.getAttachments())
            .map(requestAttachments -> requestAttachments.stream()
                .map(attachment -> WebMessageDto.AttachmentDto.builder()
                    .withFileName(attachment.getFileName())
                    .withMimeType(attachment.getMimeType())
                    .withBase64Data(attachment.getBase64Data())
                    .build())
                .toList())
            .orElse(null);

        return WebMessageDto.builder()
                .withParty(request.getParty())
                .withMessage(request.getMessage())
                .withAttachments(attachments)
                .build();
    }
}
