package se.sundsvall.messaging.service;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.request.IncomingSmsRequest;
import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.integration.sms.SmsIntegration;
import se.sundsvall.messaging.mapper.SmsMapper;
import se.sundsvall.messaging.mapper.UndeliverableMapper;
import se.sundsvall.messaging.model.dto.SmsDto;
import se.sundsvall.messaging.model.entity.SmsEntity;
import se.sundsvall.messaging.repository.SmsRepository;

@Service
public class SmsService {

    private static final Logger LOG = LoggerFactory.getLogger(SmsService.class);

    private final SmsRepository smsRepository;
    private final DefaultSettings defaultSettings;
    private final SmsIntegration smsIntegration;
    private final HistoryService historyService;

    public SmsService(SmsRepository smsRepository,
                      DefaultSettings defaultSettings,
                      SmsIntegration smsIntegration,
                      HistoryService historyService) {
        this.smsRepository = smsRepository;
        this.defaultSettings = defaultSettings;
        this.smsIntegration = smsIntegration;
        this.historyService = historyService;
    }

    public SmsDto saveSms(IncomingSmsRequest sms) {
        if (StringUtils.isBlank(sms.getSender())) {
            sms.setSender(defaultSettings.getSmsName());
        }

        return SmsMapper.toDto(smsRepository.save(SmsMapper.toEntity(sms)));
    }

    public void sendOldestPendingSms() {
        SmsEntity sms = getOldestPendingSms();

        if (sms == null) {
            return;
        }

        if (sms.getSendingAttempts() >= smsIntegration.getMessageRetries()) {
            LOG.info("Exceeded max sending attempts for SMS {}", sms.getMessageId());

            historyService.createHistory(UndeliverableMapper.toUndeliverable(sms));
            smsRepository.deleteById(sms.getMessageId());
            return;
        }

        ResponseEntity<Boolean> status;

        try {
            status = smsIntegration.sendSms(SmsMapper.toRequest(sms));
        } catch (RestClientException e) {
            LOG.warn(e.getMessage());
            status = ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(false);
        }

        if (BooleanUtils.isTrue(status.getBody()) && status.getStatusCode().equals(HttpStatus.OK)) {
            historyService.createHistory(sms);
            smsRepository.deleteById(sms.getMessageId());
        } else {
            int sendingAttempts = sms.getSendingAttempts() + 1;

            LOG.info("Unable to send SMS, current attempt {}", sendingAttempts);

            sms.setSendingAttempts(sendingAttempts);
            smsRepository.save(sms);
        }
    }

    public SmsEntity getOldestPendingSms() {
        return smsRepository.findByStatusEquals(MessageStatus.PENDING, Sort.by("createdAt").ascending())
                .stream()
                .findFirst()
                .orElse(null);
    }
}
