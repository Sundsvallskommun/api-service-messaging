package se.sundsvall.messaging.integration.emailsender;

import java.util.List;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.dto.EmailDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.processor.Processor;
import se.sundsvall.messaging.service.event.IncomingEmailEvent;

@Component
class EmailProcessor extends Processor {

    private final EmailSenderIntegration emailSenderIntegration;

    EmailProcessor(final RetryTemplate retryTemplate,
            final MessageRepository messageRepository,
            final HistoryRepository historyRepository,
            final EmailSenderIntegration emailSenderIntegration) {
        super(retryTemplate, messageRepository, historyRepository);

        this.emailSenderIntegration = emailSenderIntegration;
    }

    @EventListener(IncomingEmailEvent.class)
    void handleIncomingEmailEvent(final IncomingEmailEvent event) {
        var message = messageRepository.findById(event.getMessageId()).orElse(null);

        if (message == null) {
            return;
        }

        retryTemplate.execute(retryContext -> {
            try {
                var emailDto = mapToDto(message);
                var response = emailSenderIntegration.sendEmail(emailDto);

                if (response.getStatusCode() == HttpStatus.OK) {
                    // Success - we're done
                    handleSuccessfulDelivery(message);
                    retryContext.setExhaustedOnly();

                    return null;
                }
            } catch (Exception e) {
                log.info("Unable to send e-mail: " + e.getMessage());
            }

            throw new ProcessingException();
        }, retryContext -> {
            handleMaximumDeliveryAttemptsExceeded(message);

            return null;
        });
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
