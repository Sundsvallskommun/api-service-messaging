package se.sundsvall.messaging.integration.emailsender;

import java.util.List;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.configuration.RetryProperties;
import se.sundsvall.messaging.dto.EmailDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.processor.Processor;
import se.sundsvall.messaging.service.event.IncomingEmailEvent;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;

@Component
class EmailProcessor extends Processor {

    private final EmailSenderIntegration emailSenderIntegration;

    private final RetryPolicy<ResponseEntity<Void>> retryPolicy;

    EmailProcessor(final RetryProperties retryProperties,
            final MessageRepository messageRepository,
            final HistoryRepository historyRepository,
            final EmailSenderIntegration emailSenderIntegration) {
        super(messageRepository, historyRepository);

        this.emailSenderIntegration = emailSenderIntegration;

        retryPolicy = RetryPolicy.<ResponseEntity<Void>>builder()
            .withMaxAttempts(retryProperties.getMaxAttempts())
            .withBackoff(retryProperties.getInitialDelay(), retryProperties.getMaxDelay())
            .handle(Exception.class)
            .handleResultIf(response -> response.getStatusCode() != HttpStatus.OK)
            .onFailedAttempt(event -> log.debug("Unable to send e-mail ({}/{}): {}",
                event.getAttemptCount(), retryProperties.getMaxAttempts(), event.getLastException().getMessage()))
            .build();
    }

    @EventListener(IncomingEmailEvent.class)
    void handleIncomingEmailEvent(final IncomingEmailEvent event) {
        var message = messageRepository.findById(event.getMessageId()).orElse(null);

        if (message == null) {
            log.warn("Unable to process missing e-mail {}", event.getMessageId());

            return;
        }

        var emailDto = mapToDto(message);

        try {
            Failsafe
                .with(retryPolicy)
                .onSuccess(successEvent -> handleSuccessfulDelivery(message))
                .onFailure(failureEvent -> handleMaximumDeliveryAttemptsExceeded(message))
                .get(() -> emailSenderIntegration.sendEmail(emailDto));
        } catch (Exception e) {
            log.warn("Unable to send e-mail {}: {}", event.getMessageId(), e.getMessage());
        }
    }

    EmailDto mapToDto(final MessageEntity message) {
        var request = GSON.fromJson(message.getContent(), EmailRequest.class);

        return EmailDto.builder()
            .withSender(request.getSender())
            .withEmailAddress(request.getEmailAddress())
            .withSubject(request.getSubject())
            .withMessage(request.getMessage())
            .withHtmlMessage(request.getHtmlMessage())
            .withAttachments(Optional.ofNullable(request.getAttachments()).orElse(List.of()).stream()
                .map(attachment -> EmailDto.AttachmentDto.builder()
                    .withName(attachment.getName())
                    .withContentType(attachment.getContentType())
                    .withContent(attachment.getContent())
                    .build())
                .toList())
            .build();
    }
}