package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.request.IncomingSmsRequest;
import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.integration.sms.SmsIntegration;
import se.sundsvall.messaging.model.dto.UndeliverableMessageDto;
import se.sundsvall.messaging.model.entity.SmsEntity;
import se.sundsvall.messaging.repository.SmsRepository;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {
    
    private static final String PARTY_ID = UUID.randomUUID().toString();
    private static final String MOBILE_NUMBER = "+46701234567";

    @Mock
    private SmsRepository smsRepository;
    @Mock
    private DefaultSettings defaultSettings;
    @Mock
    private SmsIntegration smsIntegration;
    @Mock
    private HistoryService historyService;

    private SmsService smsService;

    @BeforeEach
    void setUp() {
        smsService = new SmsService(smsRepository, defaultSettings, smsIntegration, historyService);
    }

    @Test
    void saveSms_givenValidSmsRequest_thenSaveSms() {
        IncomingSmsRequest smsRequest = createSmsRequest(null);

        smsService.saveSms(smsRequest);

        verify(smsRepository, times(1)).save(any());
    }

    @Test
    void saveSms_givenSmsRequestWithoutSender_thenUseDefaultSettings() {
        IncomingSmsRequest nullSender = createSmsRequest(sms -> sms.setSender(null));
        IncomingSmsRequest blankSender = createSmsRequest(sms -> sms.setSender(" "));

        smsService.saveSms(nullSender);
        smsService.saveSms(blankSender);

        verify(defaultSettings, times(2)).getSmsName();
        verify(smsRepository, times(2)).save(any());
    }

    @Test
    void sendOldestPendingSms_whenNoPendingSms_thenNothingToSend() {
        List<SmsEntity> sms = Collections.emptyList();

        when(smsRepository.findByStatusEquals(any(), any())).thenReturn(sms);

        smsService.sendOldestPendingSms();

        verifyNoInteractions(smsIntegration, historyService);
    }

    @Test
    void sendOldestPendingSms_whenSmsExceededMaxSendAttempts_thenMoveUndeliverableToHistory() {
        List<SmsEntity> sms = List.of(createSms(message -> message.setSendingAttempts(3)));

        when(smsRepository.findByStatusEquals(any(), any())).thenReturn(sms);
        when(smsIntegration.getMessageRetries()).thenReturn(3);

        smsService.sendOldestPendingSms();

        verify(historyService, times(1)).createHistory(any(UndeliverableMessageDto.class));
        verify(smsRepository, times(1)).deleteById(anyString());
    }

    @Test
    void sendOldestPendingSms_whenAttemptingToSendSmsAndFail_thenUpdateSendingAttempts() {
        List<SmsEntity> sms = List.of(createSms(message -> message.setSendingAttempts(0)));
        ArgumentCaptor<SmsEntity> smsCaptor = ArgumentCaptor.forClass(SmsEntity.class);

        when(smsRepository.findByStatusEquals(any(), any())).thenReturn(sms);
        when(smsIntegration.getMessageRetries()).thenReturn(3);
        when(smsIntegration.sendSms(any())).thenThrow(new RestClientException("Connection refused"));

        smsService.sendOldestPendingSms();

        verify(smsRepository, times(1)).save(smsCaptor.capture());

        assertThat(smsCaptor.getValue().getSendingAttempts()).isEqualTo(1);
    }

    @Test
    void sendOldestPendingSms_whenSmsSentAndResponseStatus_OK_thenMoveToHistory() {
        List<SmsEntity> sms = List.of(createSms(null));

        when(smsRepository.findByStatusEquals(any(), any())).thenReturn(sms);
        when(smsIntegration.getMessageRetries()).thenReturn(3);
        when(smsIntegration.sendSms(any())).thenReturn(ResponseEntity.ok(true));

        smsService.sendOldestPendingSms();

        verify(historyService, times(1)).createHistory(any(SmsEntity.class));
        verify(smsRepository, times(1)).deleteById(anyString());
    }

    private SmsEntity createSms(Consumer<SmsEntity> modifier) {
        SmsEntity sms = SmsEntity.builder()
                .withBatchId(UUID.randomUUID().toString())
                .withMessageId(UUID.randomUUID().toString())
                .withPartyId(PARTY_ID)
                .withMobileNumber(MOBILE_NUMBER)
                .withSender("Sundsvall")
                .withMessage("Message content")
                .withStatus(MessageStatus.PENDING)
                .withSendingAttempts(0)
                .withCreatedAt(LocalDateTime.now())
                .build();

        if (modifier != null) {
            modifier.accept(sms);
        }

        return sms;
    }

    private IncomingSmsRequest createSmsRequest(Consumer<IncomingSmsRequest> modifier) {
        IncomingSmsRequest smsRequest = IncomingSmsRequest.builder()
                .withPartyId(PARTY_ID)
                .withMobileNumber(MOBILE_NUMBER)
                .withSender("Sundsvall")
                .withMessage("Message content")
                .build();

        if (modifier != null) {
            modifier.accept(smsRequest);
        }

        return smsRequest;
    }
}
