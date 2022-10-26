package se.sundsvall.messaging.integration.digitalmailsender;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import se.sundsvall.messaging.api.model.DigitalMailRequest;
import se.sundsvall.messaging.configuration.RetryProperties;
import se.sundsvall.messaging.dto.DigitalMailDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.ContentType;
import se.sundsvall.messaging.processor.Processor;
import se.sundsvall.messaging.service.event.IncomingDigitalMailEvent;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;

@Component
class DigitalMailProcessor extends Processor {

    private final DigitalMailSenderIntegration digitalMailSenderIntegration;

    private final RetryPolicy<ResponseEntity<Boolean>> retryPolicy;

    DigitalMailProcessor(final RetryProperties retryProperties,
            final MessageRepository messageRepository,
            final HistoryRepository historyRepository,
            final DigitalMailSenderIntegration digitalMailSenderIntegration) {
        super(messageRepository, historyRepository);

        this.digitalMailSenderIntegration = digitalMailSenderIntegration;

        retryPolicy = RetryPolicy.<ResponseEntity<Boolean>>builder()
            .withMaxAttempts(retryProperties.getMaxAttempts())
            .withBackoff(retryProperties.getInitialDelay(), retryProperties.getMaxDelay())
            .handle(Exception.class)
            .handleResultIf(response -> response.getStatusCode() != HttpStatus.OK || !BooleanUtils.isTrue(response.getBody()))
            .onFailedAttempt(event -> log.info("Unable to send digital mail ({}/{}): {}",
                event.getAttemptCount(), retryProperties.getMaxAttempts(), event.getLastException().getMessage()))
            .build();
    }

    @Transactional
    @EventListener(IncomingDigitalMailEvent.class)
    void handleIncomingDigitalMailEvent(final IncomingDigitalMailEvent event) {
        var message = messageRepository.findByDeliveryId(event.getPayload()).orElse(null);

        if (message == null) {
            log.warn("Unable to process missing digital mail {}", event.getPayload());

            return;
        }

        var digitalMailDto = mapToDto(message);

        try {
            Failsafe
                .with(retryPolicy)
                .onSuccess(successEvent -> handleSuccessfulDelivery(message))
                .onFailure(failureEvent -> handleMaximumDeliveryAttemptsExceeded(message))
                .get(() -> digitalMailSenderIntegration.sendDigitalMail(digitalMailDto));
        } catch (Exception e) {
            log.warn("Unable to send digital mail {}: {}", event.getPayload(), e.getMessage());
        }
    }

    DigitalMailDto mapToDto(final MessageEntity message) {
        var request = GSON.fromJson(message.getContent(), DigitalMailRequest.class);

        List<DigitalMailDto.AttachmentDto> attachments = null;
        if (!CollectionUtils.isEmpty(request.getAttachments())) {
            attachments = Optional.ofNullable(request.getAttachments()).stream()
                    .flatMap(Collection::stream)
                    .map(attachment -> DigitalMailDto.AttachmentDto.builder()
                            .withContentType(ContentType.fromString(attachment.getContentType()))
                            .withContent(attachment.getContent())
                            .withFilename(attachment.getFilename())
                            .build())
                    .toList();
        }
        return DigitalMailDto.builder()
                .withPartyId(message.getPartyId())
                .withSubject(request.getSubject())
                .withContentType(ContentType.fromString(request.getContentType()))
                .withBody(request.getBody())
                .withAttachments(attachments)
                .build();
    }
}
