package se.sundsvall.messaging.service;

import java.time.Duration;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.dto.EmailDto;
import se.sundsvall.messaging.integration.db.EmailRepository;
import se.sundsvall.messaging.integration.db.entity.EmailEntity;
import se.sundsvall.messaging.integration.emailsender.EmailSenderIntegration;
import se.sundsvall.messaging.integration.emailsender.EmailSenderIntegrationProperties;
import se.sundsvall.messaging.mapper.EmailMapper;
import se.sundsvall.messaging.mapper.UndeliverableMapper;
import se.sundsvall.messaging.model.MessageStatus;

@Service
public class EmailService implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

    private final DefaultSettings defaultSettings;
    private final EmailRepository emailRepository;
    private final EmailSenderIntegrationProperties emailSenderIntegrationProperties;
    private final EmailSenderIntegration emailSenderIntegration;
    private final HistoryService historyService;

    public EmailService(final DefaultSettings defaultSettings, final EmailRepository emailRepository,
            final EmailSenderIntegrationProperties emailSenderIntegrationProperties,
            final EmailSenderIntegration emailSenderIntegration, final HistoryService historyService) {
        this.defaultSettings = defaultSettings;
        this.emailRepository = emailRepository;
        this.emailSenderIntegration = emailSenderIntegration;
        this.emailSenderIntegrationProperties = emailSenderIntegrationProperties;
        this.historyService = historyService;
    }

    public Duration getPollDelay() {
        return emailSenderIntegrationProperties.getPollDelay();
    }

    public EmailDto saveEmail(final EmailRequest request) {
        if (StringUtils.isBlank(request.getSenderEmail())) {
            request.setSenderEmail(defaultSettings.getEmailAddress());
        }

        if (StringUtils.isBlank(request.getSenderName())) {
            request.setSenderName(defaultSettings.getEmailName());
        }

        return EmailMapper.toDto(emailRepository.save(EmailMapper.toEntity(request)));
    }

    @Override
    public void run() {
        LOG.trace("Polling e-mail");

        sendOldestPendingMessages();
    }

    void sendOldestPendingMessages() {
        getOldestPendingMessages().forEach(emailEntity -> {
            emailEntity.setSendingAttempts(emailEntity.getSendingAttempts() + 1);

            if (emailEntity.getSendingAttempts() > emailSenderIntegrationProperties.getMaxRetries()) {
                LOG.info("Exceeded max sending attempts for e-mail {}", emailEntity.getMessageId());

                historyService.createHistory(UndeliverableMapper.toUndeliverable(emailEntity));
                emailRepository.deleteById(emailEntity.getMessageId());
                return;
            }

            try {
                var status = emailSenderIntegration.sendEmail(EmailMapper.toDto(emailEntity));

                if (status == HttpStatus.OK) {
                    historyService.createHistory(emailEntity);
                    emailRepository.deleteById(emailEntity.getMessageId());

                    return;
                }
            } catch (Exception e) {
                LOG.info("Unable to send e-mail ({}/{}): " + e.getMessage(),
                    emailEntity.getSendingAttempts(), emailSenderIntegrationProperties.getMaxRetries());
            }

            emailRepository.save(emailEntity);
        });
    }

    List<EmailEntity> getOldestPendingMessages() {
        return emailRepository.findLatestWithStatus(MessageStatus.PENDING);
    }
}
