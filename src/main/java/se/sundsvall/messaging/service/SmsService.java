package se.sundsvall.messaging.service;

import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import se.sundsvall.messaging.api.request.IncomingSmsRequest;
import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.dto.SmsDto;
import se.sundsvall.messaging.integration.db.SmsRepository;
import se.sundsvall.messaging.integration.db.entity.SmsEntity;
import se.sundsvall.messaging.integration.smssender.SmsSenderIntegration;
import se.sundsvall.messaging.mapper.SmsMapper;
import se.sundsvall.messaging.mapper.UndeliverableMapper;
import se.sundsvall.messaging.model.MessageStatus;

@Service
public class SmsService implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(SmsService.class);

    private final SmsRepository smsRepository;
    private final DefaultSettings defaultSettings;
    private final SmsSenderIntegration smsIntegration;
    private final HistoryService historyService;

    public SmsService(SmsRepository smsRepository,
                      DefaultSettings defaultSettings,
                      SmsSenderIntegration smsIntegration,
                      HistoryService historyService) {
        this.smsRepository = smsRepository;
        this.defaultSettings = defaultSettings;
        this.smsIntegration = smsIntegration;
        this.historyService = historyService;
    }

    public SmsDto saveSms(final IncomingSmsRequest sms) {
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
        getOldestPendingMessages().forEach(message -> {
            if (message.getSendingAttempts() >= smsIntegration.getMessageRetries()) {
                LOG.info("Exceeded max sending attempts for SMS {}", message.getMessageId());

                historyService.createHistory(UndeliverableMapper.toUndeliverable(message));
                smsRepository.deleteById(message.getMessageId());
                return;
            }

            try {
                var response = smsIntegration.sendSms(SmsMapper.toDto(message));

                if (response.getStatusCode() == HttpStatus.OK && BooleanUtils.isTrue(response.getBody())) {
                    historyService.createHistory(message);
                    smsRepository.deleteById(message.getMessageId());
                } else {
                    updateSendingAttempts(message);
                }
            } catch (Exception e) {
                updateSendingAttempts(message);
            }
        });
    }

    void updateSendingAttempts(final SmsEntity smsEntity) {
        int sendingAttempts = smsEntity.getSendingAttempts() + 1;
        LOG.info("Unable to send SMS, current attempt {}", sendingAttempts);

        smsEntity.setSendingAttempts(sendingAttempts);
        smsRepository.save(smsEntity);
    }

    List<SmsEntity> getOldestPendingMessages() {
        return smsRepository.findByStatusEquals(MessageStatus.PENDING, Sort.by("createdAt").ascending());
    }
}
