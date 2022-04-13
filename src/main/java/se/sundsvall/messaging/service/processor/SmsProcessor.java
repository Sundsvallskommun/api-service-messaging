package se.sundsvall.messaging.service.processor;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.dto.SmsDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.smssender.SmsSenderIntegration;
import se.sundsvall.messaging.service.event.IncomingSmsEvent;

@Component
public class SmsProcessor extends Processor {

    private final SmsSenderIntegration smsSenderIntegration;

    public SmsProcessor(final RetryTemplate retryTemplate,
            final MessageRepository messageRepository,
            final HistoryRepository historyRepository,
            final SmsSenderIntegration smsSenderIntegration) {
        super(retryTemplate, messageRepository, historyRepository);

        this.smsSenderIntegration = smsSenderIntegration;
    }

    @EventListener(IncomingSmsEvent.class)
    public void handleIncomingSmsEvent(final IncomingSmsEvent event) {
        var message = messageRepository.findById(event.getMessageId()).orElse(null);

        if (message == null) {
            return;
        }

        retryTemplate.execute(retryContext -> {
            try {
                var smsDto = mapToDto(message);
                var response = smsSenderIntegration.sendSms(smsDto);

                if (response.getStatusCode() == HttpStatus.OK && BooleanUtils.isTrue(response.getBody())) {
                    // Success - we're done
                    handleSuccessfulDelivery(message);
                    retryContext.setExhaustedOnly();

                    return null;
                }
            } catch (Exception e) {
                log.info("Unable to send SMS: " + e.getMessage());
            }

            throw new ProcessingException();
        }, retryContext -> {
            handleMaximumDeliveryAttemptsExceeded(message);

            return null;
        });
    }

    SmsDto mapToDto(final MessageEntity message) {
        var request = GSON.fromJson(message.getContent(), SmsRequest.class);

        return SmsDto.builder()
            .withSender(request.getSender())
            .withMobileNumber(request.getMobileNumber())
            .withMessage(request.getMessage())
            .build();
    }
}
