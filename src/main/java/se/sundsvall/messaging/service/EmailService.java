package se.sundsvall.messaging.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.request.IncomingEmailRequest;
import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.integration.email.EmailIntegration;
import se.sundsvall.messaging.mapper.EmailMapper;
import se.sundsvall.messaging.mapper.UndeliverableMapper;
import se.sundsvall.messaging.model.dto.EmailDto;
import se.sundsvall.messaging.model.entity.EmailEntity;
import se.sundsvall.messaging.repository.EmailRepository;

@Service
public class EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

    private final EmailRepository emailRepository;
    private final DefaultSettings defaultSettings;
    private final EmailIntegration emailIntegration;
    private final HistoryService historyService;

    public EmailService(EmailRepository emailRepository,
                        DefaultSettings defaultSettings,
                        EmailIntegration emailIntegration,
                        HistoryService historyService) {
        this.emailRepository = emailRepository;
        this.defaultSettings = defaultSettings;
        this.emailIntegration = emailIntegration;
        this.historyService = historyService;
    }

    public EmailDto saveEmail(IncomingEmailRequest email) {
        if (StringUtils.isBlank(email.getSenderEmail())) {
            email.setSenderEmail(defaultSettings.getEmailAddress());
        }

        if (StringUtils.isBlank(email.getSenderName())) {
            email.setSenderName(defaultSettings.getEmailName());
        }

        return EmailMapper.toDto(emailRepository.save(EmailMapper.toEntity(email)));
    }

    public void sendOldestPendingEmail() {
        EmailEntity email = getOldestPendingEmail();

        if (email == null) {
            return;
        }

        if (email.getSendingAttempts() >= emailIntegration.getMessageRetries()) {
            LOG.info("Exceeded max sending attempts for Email {}", email.getMessageId());

            historyService.createHistory(UndeliverableMapper.toUndeliverable(email));
            emailRepository.deleteById(email.getMessageId());
            return;
        }

        HttpStatus status = emailIntegration.sendEmail(EmailMapper.toRequest(email));

        if (status == HttpStatus.OK) {
            historyService.createHistory(email);
            emailRepository.deleteById(email.getMessageId());
        } else {
            int sendingAttempts = email.getSendingAttempts() + 1;
            LOG.info("Unable to send Email, current attempt {}", sendingAttempts);

            email.setSendingAttempts(sendingAttempts);
            emailRepository.save(email);
        }

    }

    public EmailEntity getOldestPendingEmail() {
        return emailRepository.findByStatusEquals(MessageStatus.PENDING, Sort.by("createdAt").ascending())
                .stream()
                .findFirst()
                .orElse(null);
    }
}
