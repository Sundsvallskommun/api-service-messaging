package se.sundsvall.messaging.integration.digitalmailsender;

import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.messaging.api.model.DigitalMailRequest;
import se.sundsvall.messaging.configuration.Defaults;
import se.sundsvall.messaging.configuration.RetryProperties;
import se.sundsvall.messaging.dto.DigitalMailDto;
import se.sundsvall.messaging.integration.db.CounterRepository;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.ContentType;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.processor.Processor;
import se.sundsvall.messaging.service.WhitelistingService;
import se.sundsvall.messaging.service.event.IncomingDigitalMailEvent;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;

@Component
class DigitalMailProcessor extends Processor {

    private final DigitalMailSenderIntegration digitalMailSenderIntegration;
    private final Defaults defaults;

    private final RetryPolicy<ResponseEntity<Boolean>> retryPolicy;

    DigitalMailProcessor(final RetryProperties retryProperties,
            final MessageRepository messageRepository,
            final HistoryRepository historyRepository,
            final CounterRepository counterRepository,
            final WhitelistingService whitelistingService,
            final DigitalMailSenderIntegration digitalMailSenderIntegration,
            final Defaults defaults) {
        super(messageRepository, historyRepository, counterRepository, whitelistingService);

        this.digitalMailSenderIntegration = digitalMailSenderIntegration;
        this.defaults = defaults;

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

        // Register a delivery attempt
        incrementAttemptCounter(MessageType.DIGITAL_MAIL);

        var digitalMailDto = mapToDto(message);

        // Check if the recipient is whitelisted
        if (!isWhitelisted(message, digitalMailDto.getPartyId())) {
            return;
        }

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

        // Use sender from the original request, or use default sender as fallback
        var sender = Optional.ofNullable(request.getSender()).orElseGet(defaults::getDigitalMail);
        // Always use the municipality id from the defaults
        sender.setMunicipalityId(defaults.getDigitalMail().getMunicipalityId());
        // Use subject from the original request, or use default subject as fallback
        var subject = Optional.ofNullable(request.getSubject())
            .orElseGet(() -> defaults.getDigitalMail().getSubject());

        var attachments = Optional.ofNullable(request.getAttachments())
            .map(requestAttachments -> requestAttachments.stream()
                .map(attachment -> DigitalMailDto.AttachmentDto.builder()
                    .withContentType(ContentType.fromString(attachment.getContentType()))
                    .withContent(attachment.getContent())
                    .withFilename(attachment.getFilename())
                    .build())
                .toList())
            .orElse(null);

        return DigitalMailDto.builder()
            .withSender(sender)
            .withPartyId(message.getPartyId())
            .withSubject(subject)
            .withContentType(ContentType.fromString(request.getContentType()))
            .withBody(request.getBody())
            .withAttachments(attachments)
            .build();
    }
}
