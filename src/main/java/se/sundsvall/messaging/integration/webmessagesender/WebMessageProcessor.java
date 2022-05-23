package se.sundsvall.messaging.integration.webmessagesender;

import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.api.model.WebMessageRequest;
import se.sundsvall.messaging.dto.WebMessageDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.processor.Processor;
import se.sundsvall.messaging.service.event.IncomingWebMessageEvent;

@Component
class WebMessageProcessor extends Processor {

    private final WebMessageSenderIntegration webMessageSenderIntegration;

    WebMessageProcessor(final RetryTemplate retryTemplate,
            final MessageRepository messageRepository,
            final HistoryRepository historyRepository,
            final WebMessageSenderIntegration webMessageSenderIntegration) {
        super(retryTemplate, messageRepository, historyRepository);

        this.webMessageSenderIntegration = webMessageSenderIntegration;
    }

    @EventListener(IncomingWebMessageEvent.class)
    void handleIncomingWebMessageEvent(final IncomingWebMessageEvent event) {
        var message = messageRepository.findById(event.getMessageId()).orElse(null);

        if (message == null) {
            return;
        }

        retryTemplate.execute(retryContext -> {
            try {
                var webMessageDto = mapToDto(message);
                var response = webMessageSenderIntegration.sendWebMessage(webMessageDto);

                if (response.getStatusCode() == HttpStatus.CREATED) {
                    // Success - we're done
                    handleSuccessfulDelivery(message);
                    retryContext.setExhaustedOnly();

                    return null;
                }
            } catch (Exception e) {
                log.info("Unable to send web message: " + e.getMessage());
            }

            throw new ProcessingException();
        }, retryContext -> {
            handleMaximumDeliveryAttemptsExceeded(message);

            return null;
        });
    }

    WebMessageDto mapToDto(final MessageEntity message) {
        var request = GSON.fromJson(message.getContent(), WebMessageRequest.class);

        return WebMessageDto.builder()
            .withParty(request.getParty())
            .withMessage(request.getMessage())
            .build();
    }
}
