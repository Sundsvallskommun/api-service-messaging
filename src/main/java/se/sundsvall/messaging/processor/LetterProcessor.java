package se.sundsvall.messaging.processor;


import static se.sundsvall.messaging.api.model.LetterRequest.DeliveryMode.DIGITAL;
import static se.sundsvall.messaging.api.model.LetterRequest.DeliveryMode.SNAIL;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.messaging.api.model.DigitalMailRequest;
import se.sundsvall.messaging.api.model.LetterRequest;
import se.sundsvall.messaging.api.model.SnailmailRequest;
import se.sundsvall.messaging.configuration.Defaults;
import se.sundsvall.messaging.configuration.RetryProperties;
import se.sundsvall.messaging.dto.DigitalMailDto;
import se.sundsvall.messaging.dto.SnailmailDto;
import se.sundsvall.messaging.integration.db.CounterRepository;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.digitalmailsender.DigitalMailSenderIntegration;
import se.sundsvall.messaging.integration.snailmailsender.SnailmailSenderIntegration;
import se.sundsvall.messaging.model.ContentType;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.event.IncomingLetterEvent;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;

@Component
public class LetterProcessor extends Processor {

    private final Defaults defaults;
    private final RetryProperties retryProperties;
    private final DigitalMailSenderIntegration digitalMailSenderIntegration;
    private final SnailmailSenderIntegration snailmailSenderIntegration;
    private final RetryPolicy<ResponseEntity<Boolean>> digitalMailRetryPolicy;
    private final RetryPolicy<ResponseEntity<Void>> snailMailRetryPolicy;

    protected LetterProcessor(final RetryProperties retryProperties,
            final MessageRepository messageRepository,
            final HistoryRepository historyRepository,
            final CounterRepository counterRepository,
            final Defaults defaults, final DigitalMailSenderIntegration digitalMailSenderIntegration,
            final SnailmailSenderIntegration snailmailSenderIntegration) {
        super(messageRepository, historyRepository, counterRepository);

        this.retryProperties = retryProperties;
        this.defaults = defaults;
        this.digitalMailSenderIntegration = digitalMailSenderIntegration;
        this.snailmailSenderIntegration = snailmailSenderIntegration;

        digitalMailRetryPolicy = createRetryPolicy(Boolean.class);
        snailMailRetryPolicy = createRetryPolicy(Void.class);
    }

    @Transactional
    @EventListener(IncomingLetterEvent.class)
    public void handleIncomingLetterEvent(final IncomingLetterEvent event) {
        var message = messageRepository.findByDeliveryId(event.getPayload()).orElse(null);

        if (message == null) {
            log.warn("Unable to process missing letter {}", event.getPayload());
            return;
        }

        // Register a LETTER delivery attempt
        incrementAttemptCounter(MessageType.LETTER);

        var digitalAttachments = GSON.fromJson(message.getContent(), LetterRequest.class)
            .getAttachments().stream()
            .filter(attachment -> attachment.getDeliveryMode().equals(DIGITAL))
            .toList();

        sendDigitalMail(message, event, digitalAttachments);
    }

    private void sendDigitalMail(final MessageEntity message, final IncomingLetterEvent event,
            final List<LetterRequest.Attachment> attachments) {
        var dto = mapToDigitalMailDto(message, attachments);

        // Register a DIGITAL_MAIL delivery attempt
        incrementAttemptCounter(MessageType.DIGITAL_MAIL);

        try {
            Failsafe
                .with(digitalMailRetryPolicy)
                .onSuccess(successEvent -> {
                    // Register a DIGITAL_MAIL delivery success
                    incrementSuccessCounter(MessageType.DIGITAL_MAIL);
                    // Handle successful delivery, incl. LETTER success
                    handleSuccessfulDelivery(message);
                })
                .onFailure(failureEvent -> {
                    // Register a DIGITAL_MAIL failure
                    incrementFailureCounter(MessageType.DIGITAL_MAIL);
                    // Register a LETTER failover
                    incrementCounter(MessageType.LETTER.toString().toLowerCase() + ".failover");
                    // Switch over to snail-mail
                    sendSnailMail(message, event);
                })
                .get(() -> digitalMailSenderIntegration.sendDigitalMail(dto));
        } catch (Exception e) {
            log.warn("Unable to send digital mail via letter {}: {}", event.getPayload(),
                e.getMessage());
        }
    }

    private void sendSnailMail(final MessageEntity message, final IncomingLetterEvent event) {
        var snailAndFailedAttachments = GSON.fromJson(message.getContent(), LetterRequest.class)
            .getAttachments().stream()
            .filter(attachment -> attachment.getDeliveryMode().equals(SNAIL))
            .toList();

        if (snailAndFailedAttachments.isEmpty()) {
            handleMaximumDeliveryAttemptsExceeded(message);
        } else {
            sendSnailMail(message, event, snailAndFailedAttachments);
        }
    }

    private void sendSnailMail(final MessageEntity message, final IncomingLetterEvent event,
            final List<LetterRequest.Attachment> snailAndFail) {
        var dto = mapToSnailmailDto(message, snailAndFail);

        // Register a SNAIL_MAIL delivery attempt
        incrementAttemptCounter(MessageType.SNAIL_MAIL);

        try {
            Failsafe
                .with(snailMailRetryPolicy)
                .onSuccess(successEvent -> {
                    // Register a SNAIL_MAIL delivery success
                    incrementSuccessCounter(MessageType.SNAIL_MAIL);
                    // Handle successful delivery, incl. LETTER success
                    handleSuccessfulDelivery(message);
                })
                .onFailure(failureEvent -> {
                    // Register a SNAIL_MAIL failure
                    incrementFailureCounter(MessageType.SNAIL_MAIL);

                    handleMaximumDeliveryAttemptsExceeded(message);
                })
                .get(() -> snailmailSenderIntegration.sendSnailmail(dto));
        } catch (Exception e) {
            log.warn("Unable to send snailMail via letter {}: {}", event.getPayload(), e.getMessage());
        }
    }

    private DigitalMailDto mapToDigitalMailDto(final MessageEntity message,
            final List<LetterRequest.Attachment> attachmentsForDigitalMail) {
        var request = GSON.fromJson(message.getContent(), DigitalMailRequest.class);
        // Use sender from the original request, or use default sender as fallback
        var sender = Optional.ofNullable(request.getSender()).orElseGet(defaults::getDigitalMail);
        // Always use the municipality id from the defaults
        sender.setMunicipalityId(defaults.getDigitalMail().getMunicipalityId());
        // Use subject from the original request, or use default subject as fallback
        var subject = Optional.ofNullable(request.getSubject())
            .orElseGet(() -> defaults.getDigitalMail().getSubject());

        return DigitalMailDto.builder()
            .withPartyId(message.getPartyId())
            .withSubject(subject)
            .withSender(sender)
            .withContentType(ContentType.fromString(request.getContentType()))
            .withBody(request.getBody())
            .withAttachments(Optional.ofNullable(attachmentsForDigitalMail)
                .map(attachments -> attachments.stream()
                    .map(attachment -> DigitalMailDto.AttachmentDto.builder()
                        .withContentType(ContentType.fromString(attachment.getContentType()))
                        .withContent(attachment.getContent())
                        .withFilename(attachment.getFilename())
                        .build())
                    .toList())
                .orElse(null))
            .build();
    }

    private SnailmailDto mapToSnailmailDto(final MessageEntity message,
            final List<LetterRequest.Attachment> attachmentsForSnailMail) {
        var request = GSON.fromJson(message.getContent(), SnailmailRequest.class);

        return SnailmailDto.builder()
            .withDepartment(request.getDepartment())
            .withDeviation(request.getDeviation())
            .withPersonId(request.getPersonId())
            .withAttachments(Optional.ofNullable(attachmentsForSnailMail)
                .map(attachments -> attachments.stream()
                    .map(attachment -> SnailmailDto.AttachmentDto.builder()
                        .withName(attachment.getFilename())
                        .withContentType(attachment.getContentType())
                        .withContent(attachment.getContent())
                        .build())
                    .toList())
                .orElse(null))
            .build();
    }

    private <T> RetryPolicy<ResponseEntity<T>> createRetryPolicy(final Class<T> abc) {
        var request = RetryPolicy.<ResponseEntity<T>>builder()
            .withMaxAttempts(retryProperties.getMaxAttempts())
            .withBackoff(retryProperties.getInitialDelay(), retryProperties.getMaxDelay())
            .handle(Exception.class)
            .onFailedAttempt(event -> log.debug("Unable to send letter ({}/{}): {}",
                event.getAttemptCount(), retryProperties.getMaxAttempts(),
                event.getLastException().getMessage()));

        if (abc.isNestmateOf(Boolean.class)) {
            return request.handleResultIf(response -> response.getStatusCode() != HttpStatus.OK
                    || !BooleanUtils.isTrue((Boolean) response.getBody())).build();
        } else {
            return request.handleResultIf(response -> response.getStatusCode() != HttpStatus.OK).build();
        }
    }
}
