package se.sundsvall.messaging.integration.digitalmailsender;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.api.model.DigitalMailRequest;
import se.sundsvall.messaging.dto.DigitalMailDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.ContentType;
import se.sundsvall.messaging.processor.Processor;
import se.sundsvall.messaging.service.event.IncomingDigitalMailEvent;

@Component
class DigitalMailProcessor extends Processor {

    private final DigitalMailSenderIntegration digitalMailSenderIntegration;

    DigitalMailProcessor(final RetryTemplate retryTemplate,
            final MessageRepository messageRepository,
            final HistoryRepository historyRepository,
            final DigitalMailSenderIntegration digitalMailSenderIntegration) {
        super(retryTemplate, messageRepository, historyRepository);

        this.digitalMailSenderIntegration = digitalMailSenderIntegration;
    }

    @EventListener(IncomingDigitalMailEvent.class)
    void handleIncomingDigitalMailEvent(final IncomingDigitalMailEvent event) {
        var message = messageRepository.findById(event.getMessageId()).orElse(null);

        if (message == null) {
            return;
        }

        retryTemplate.execute(retryContext -> {
            try {
                var digitalMailDto = mapToDto(message);
                var response = digitalMailSenderIntegration.sendDigitalMail(digitalMailDto);

                if (response.getStatusCode() == HttpStatus.OK && BooleanUtils.isTrue(response.getBody())) {
                    // Success - we're done
                    handleSuccessfulDelivery(message);
                    retryContext.setExhaustedOnly();

                    return null;
                }
            } catch (Exception e) {
                log.info("Unable to send digital mail: " + e.getMessage());
            }

            throw new ProcessingException();
        }, retryContext -> {
            handleMaximumDeliveryAttemptsExceeded(message);

            return null;
        });
    }

    DigitalMailDto mapToDto(final MessageEntity message) {
        var request = GSON.fromJson(message.getContent(), DigitalMailRequest.class);

        return DigitalMailDto.builder()
            .withPartyId(message.getPartyId())
            .withSubject(request.getSubject())
            .withContentType(ContentType.fromString(request.getContentType()))
            .withBody(request.getBody())
            .withAttachments(Optional.ofNullable(request.getAttachments()).orElse(List.of()).stream()
                .map(attachment -> DigitalMailDto.AttachmentDto.builder()
                    .withContentType(ContentType.fromString(attachment.getContentType()))
                    .withContent(attachment.getContent())
                    .withFilename(attachment.getFilename())
                    .build())
                .toList())
            .build();
    }
}
