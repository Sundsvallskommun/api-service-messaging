package se.sundsvall.messaging.service;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import se.sundsvall.messaging.api.model.WebMessageRequest;
import se.sundsvall.messaging.dto.WebMessageDto;
import se.sundsvall.messaging.integration.db.WebMessageRepository;
import se.sundsvall.messaging.integration.db.entity.WebMessageEntity;
import se.sundsvall.messaging.integration.webmessagesender.WebMessageSenderIntegration;
import se.sundsvall.messaging.integration.webmessagesender.WebMessageSenderIntegrationProperties;
import se.sundsvall.messaging.mapper.UndeliverableMapper;
import se.sundsvall.messaging.mapper.WebMessageMapper;
import se.sundsvall.messaging.model.MessageStatus;

@Service
public class WebMessageService implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(WebMessageService.class);

    private final WebMessageRepository webMessageRepository;
    private final WebMessageSenderIntegrationProperties webMessageSenderIntegrationProperties;
    private final WebMessageSenderIntegration webMessageSenderIntegration;
    private final HistoryService historyService;

    public WebMessageService(final WebMessageRepository webMessageRepository,
            final WebMessageSenderIntegrationProperties webMessageSenderIntegrationProperties,
            final WebMessageSenderIntegration webMessageSenderIntegration,
            final HistoryService historyService) {
        this.webMessageRepository = webMessageRepository;
        this.webMessageSenderIntegrationProperties = webMessageSenderIntegrationProperties;
        this.webMessageSenderIntegration = webMessageSenderIntegration;
        this.historyService = historyService;
    }

    public Duration getPollDelay() {
        return webMessageSenderIntegrationProperties.getPollDelay();
    }

    public WebMessageDto saveWebMessage(final WebMessageRequest sms) {
        return WebMessageMapper.toDto(webMessageRepository.save(WebMessageMapper.toEntity(sms)));
    }

    @Override
    public void run() {
        LOG.trace("Polling WebMessages");

        sendOldestPendingMessages();
    }

    void sendOldestPendingMessages() {
        getOldestPendingMessages().forEach(webMessageEntity -> {
            webMessageEntity.setSendingAttempts(webMessageEntity.getSendingAttempts() + 1);

            if (webMessageEntity.getSendingAttempts() > webMessageSenderIntegrationProperties.getMaxRetries()) {
                LOG.info("Exceeded max sending attempts for WebMessage {}", webMessageEntity.getMessageId());

                historyService.createHistory(UndeliverableMapper.toUndeliverable(webMessageEntity));
                webMessageRepository.deleteById(webMessageEntity.getMessageId());
                return;
            }

            try {
                var response = webMessageSenderIntegration.sendWebMessage(
                    WebMessageMapper.toDto(webMessageEntity));

                if (response.getStatusCode() == HttpStatus.CREATED) {
                    historyService.createHistory(webMessageEntity);
                    webMessageRepository.deleteById(webMessageEntity.getMessageId());

                    return;
                }
            } catch (Exception e) {
                LOG.info("Unable to send WebMessage ({}/{}): " + e.getMessage(),
                    webMessageEntity.getSendingAttempts(), webMessageSenderIntegrationProperties.getMaxRetries());
            }

            webMessageRepository.save(webMessageEntity);
        });
    }

    List<WebMessageEntity> getOldestPendingMessages() {
        return webMessageRepository.findLatestWithStatus(MessageStatus.PENDING);
    }
}
