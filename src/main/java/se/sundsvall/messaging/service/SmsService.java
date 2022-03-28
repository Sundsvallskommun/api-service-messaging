package se.sundsvall.messaging.service;

import java.time.Duration;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.dto.SmsDto;
import se.sundsvall.messaging.integration.db.SmsRepository;
import se.sundsvall.messaging.integration.db.entity.SmsEntity;
import se.sundsvall.messaging.integration.smssender.SmsSenderIntegration;
import se.sundsvall.messaging.integration.smssender.SmsSenderIntegrationProperties;
import se.sundsvall.messaging.mapper.SmsMapper;
import se.sundsvall.messaging.mapper.UndeliverableMapper;
import se.sundsvall.messaging.model.MessageStatus;

@Service
public class SmsService implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(SmsService.class);

    private final SmsRepository smsRepository;
    private final DefaultSettings defaultSettings;
    private final SmsSenderIntegrationProperties smsSenderIntegrationProperties;
    private final SmsSenderIntegration smsSenderIntegration;
    private final HistoryService historyService;

    public SmsService(final DefaultSettings defaultSettings,
            final SmsSenderIntegrationProperties smsSenderIntegrationProperties,
            final SmsSenderIntegration smsSenderIntegration, final SmsRepository smsRepository,
            final HistoryService historyService) {
        this.defaultSettings = defaultSettings;
        this.smsSenderIntegrationProperties = smsSenderIntegrationProperties;
        this.smsSenderIntegration = smsSenderIntegration;
        this.smsRepository = smsRepository;
        this.historyService = historyService;
    }

    public Duration getPollDelay() {
        return smsSenderIntegrationProperties.getPollDelay();
    }

    public SmsDto saveSms(final SmsRequest sms) {
        if (StringUtils.isBlank(sms.getSender())) {
            sms.setSender(defaultSettings.getSmsName());
        }

        return SmsMapper.toDto(smsRepository.save(SmsMapper.toEntity(sms)));
    }

    @Override
    public void run() {
        LOG.trace("Polling SMS");

        sendOldestPendingMessages();
    }

    void sendOldestPendingMessages() {
        getOldestPendingMessages().forEach(smsEntity -> {
            smsEntity.setSendingAttempts(smsEntity.getSendingAttempts() + 1);

            if (smsEntity.getSendingAttempts() > smsSenderIntegrationProperties.getMaxRetries()) {
                LOG.info("Exceeded max sending attempts for SMS {}", smsEntity.getMessageId());

                historyService.createHistory(UndeliverableMapper.toUndeliverable(smsEntity));
                smsRepository.deleteById(smsEntity.getMessageId());
                return;
            }

            try {
                var response = smsSenderIntegration.sendSms(SmsMapper.toDto(smsEntity));

                if (response.getStatusCode() == HttpStatus.OK && BooleanUtils.isTrue(response.getBody())) {
                    historyService.createHistory(smsEntity);
                    smsRepository.deleteById(smsEntity.getMessageId());

                    return;
                }
            } catch (Exception e) {
                LOG.info("Unable to send SMS ({}/{}): " + e.getMessage(),
                    smsEntity.getSendingAttempts(), smsSenderIntegrationProperties.getMaxRetries());
            }

            smsRepository.save(smsEntity);
        });
    }

    List<SmsEntity> getOldestPendingMessages() {
        return smsRepository.findLatestWithStatus(MessageStatus.PENDING);
    }
}
