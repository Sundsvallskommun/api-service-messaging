package se.sundsvall.messaging.integration.snailmailsender;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.messaging.api.model.SnailmailRequest;
import se.sundsvall.messaging.configuration.RetryProperties;
import se.sundsvall.messaging.dto.SnailmailDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.processor.Processor;
import se.sundsvall.messaging.service.event.IncomingSnailmailEvent;

import java.util.List;
import java.util.Optional;

@Component
class SnailmailProcessor extends Processor {

    private final SnailmailSenderIntegration snailmailSenderIntegration;
    private final RetryPolicy<ResponseEntity<Void>> retryPolicy;

    SnailmailProcessor(final RetryProperties retryProperties,
                       final MessageRepository messageRepository,
                       final HistoryRepository historyRepository,
                       final SnailmailSenderIntegration snailmailSenderIntegration) {
        super(messageRepository, historyRepository);

        this.snailmailSenderIntegration = snailmailSenderIntegration;


        retryPolicy = RetryPolicy.<ResponseEntity<Void>>builder()
                .withMaxAttempts(retryProperties.getMaxAttempts())
                .withBackoff(retryProperties.getInitialDelay(), retryProperties.getMaxDelay())
                .handle(Exception.class)
                .handleResultIf(response -> response.getStatusCode() != HttpStatus.OK)
                .onFailedAttempt(event -> log.debug("Unable to send snailmail ({}/{}): {}",
                        event.getAttemptCount(), retryProperties.getMaxAttempts(), event.getLastException().getMessage()))
                .build();
    }

    @Transactional
    @EventListener(IncomingSnailmailEvent.class)
    void handleIncomingSnailmailEvent(final IncomingSnailmailEvent event) {
        var message = messageRepository.findByDeliveryId(event.getPayload()).orElse(null);

        if (message == null) {
            log.warn("Unable to process missing snailmail {}", event.getPayload());

            return;
        }

        var snailmailDto = mapToDto(message);

        try {
            Failsafe
                    .with(retryPolicy)
                    .onSuccess(successEvent -> handleSuccessfulDelivery(message))
                    .onFailure(failureEvent -> handleMaximumDeliveryAttemptsExceeded(message))
                    .get(() -> snailmailSenderIntegration.sendSnailmail(snailmailDto));
        } catch (Exception e) {
            log.warn("Unable to send snailmail {}: {}", event.getPayload(), e.getMessage());
        }
    }

    SnailmailDto mapToDto(final MessageEntity message) {
        var request = GSON.fromJson(message.getContent(), SnailmailRequest.class);


        return SnailmailDto.builder()
                .withPersonId(request.getPersonId())
                .withAttachments(Optional.ofNullable(request.getAttachments()).orElse(List.of()).stream()
                        .map(attachment -> SnailmailDto.AttachmentDto.builder()
                                .withName(attachment.getName())
                                .withContentType(attachment.getContentType())
                                .withContent(attachment.getContent())
                                .build())
                        .toList())
                .build();
    }
}