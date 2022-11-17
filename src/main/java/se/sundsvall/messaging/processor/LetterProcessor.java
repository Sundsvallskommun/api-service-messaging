package se.sundsvall.messaging.processor;

import static se.sundsvall.messaging.model.DeliveryMode.DIGITAL;
import static se.sundsvall.messaging.model.DeliveryMode.SNAIL;

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
import se.sundsvall.messaging.configuration.RetryProperties;
import se.sundsvall.messaging.dto.DigitalMailDto;
import se.sundsvall.messaging.dto.SnailmailDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.digitalmailsender.DigitalMailSenderIntegration;
import se.sundsvall.messaging.integration.snailmailsender.SnailmailSenderIntegration;
import se.sundsvall.messaging.model.ContentType;
import se.sundsvall.messaging.service.event.IncomingLetterEvent;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;

@Component
public class LetterProcessor extends Processor {

    private final RetryProperties retryProperties;
    private final DigitalMailSenderIntegration digitalMailSenderIntegration;
    private final SnailmailSenderIntegration snailmailSenderIntegration;

    protected LetterProcessor(final RetryProperties retryProperties,
            final MessageRepository messageRepository,
            final HistoryRepository historyRepository,
            final DigitalMailSenderIntegration digitalMailSenderIntegration,
            final SnailmailSenderIntegration snailmailSenderIntegration) {
        super(messageRepository, historyRepository);
        this.retryProperties = retryProperties;
        this.digitalMailSenderIntegration = digitalMailSenderIntegration;
        this.snailmailSenderIntegration = snailmailSenderIntegration;
    }

    @Transactional
    @EventListener(IncomingLetterEvent.class)
    public void handleIncomingLetterEvent(final IncomingLetterEvent event) {

        var message = messageRepository.findByDeliveryId(event.getPayload()).orElse(null);

        if (message == null) {
            log.warn("Unable to process missing letter {}", event.getPayload());
            return;
        }

        var digitalAttachments = GSON.fromJson(message.getContent(), LetterRequest.class)
                .getAttachments().stream()
                .filter(attachment -> attachment.getDeliveryMode().equals(DIGITAL))
                .toList();

        sendDigitalMail(message, event, digitalAttachments);

    }

    private void sendDigitalMail(MessageEntity message, IncomingLetterEvent event,
            List<LetterRequest.Attachment> attachments) {

        var dto = mapToDigitalMailDto(message, attachments);

        try {
            Failsafe
                    .with(createRetryPolicy(Boolean.class))
                    .onSuccess(successEvent -> handleSuccessfulDelivery(message))
                    .onFailure(failureEvent -> digitalFailTrySnail(message, event))
                    .get(() -> digitalMailSenderIntegration.sendDigitalMail(dto));
        } catch (Exception e) {
            log.warn("Unable to send digital mail via letter {}: {}", event.getPayload(),
                    e.getMessage());
        }

    }

    private void digitalFailTrySnail(MessageEntity message, IncomingLetterEvent event) {
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


    private void sendSnailMail(MessageEntity message, IncomingLetterEvent event,
            List<LetterRequest.Attachment> snailAndFail) {

        var dto = mapToSnailmailDto(message, snailAndFail);

        try {
            Failsafe
                    .with(createRetryPolicy(Void.class))
                    .onSuccess(successEvent -> handleSuccessfulDelivery(message))
                    .onFailure(failureEvent -> handleMaximumDeliveryAttemptsExceeded(message))
                    .get(() -> snailmailSenderIntegration.sendSnailmail(dto));
        } catch (Exception e) {
            log.warn("Unable to send snailMail via letter {}: {}", event.getPayload(), e.getMessage());
        }
    }

    private DigitalMailDto mapToDigitalMailDto(final MessageEntity message,
            List<LetterRequest.Attachment> digitalAttachments) {

        var request = GSON.fromJson(message.getContent(), DigitalMailRequest.class);

        return DigitalMailDto.builder()
                .withPartyId(message.getPartyId())
                .withSubject(request.getSubject())
                .withContentType(ContentType.fromString(request.getContentType()))
                .withBody(request.getBody())
                .withAttachments(Optional.ofNullable(digitalAttachments)
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
            List<LetterRequest.Attachment> snailAndFailed) {

        var request = GSON.fromJson(message.getContent(), SnailmailRequest.class);

        return SnailmailDto.builder()
                .withDepartment(request.getDepartment())
                .withDeviation(request.getDeviation())
                .withPersonId(request.getPersonId())
                .withAttachments(Optional.ofNullable(snailAndFailed)
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


    private <T> RetryPolicy<ResponseEntity<T>> createRetryPolicy(Class<T> abc) {
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
