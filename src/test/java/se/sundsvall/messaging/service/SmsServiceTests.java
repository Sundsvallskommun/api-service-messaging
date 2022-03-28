package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.dto.UndeliverableMessageDto;
import se.sundsvall.messaging.integration.db.SmsRepository;
import se.sundsvall.messaging.integration.db.entity.SmsEntity;
import se.sundsvall.messaging.integration.smssender.SmsSenderIntegration;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.Party;

@ExtendWith(MockitoExtension.class)
class SmsServiceTests {
    
    private static final String PARTY_ID = UUID.randomUUID().toString();
    private static final String MOBILE_NUMBER = "+46701234567";

    @Mock
    private SmsRepository mockSmsRepository;
    @Mock
    private DefaultSettings mockDefaultSettings;
    @Mock
    private SmsSenderIntegration mockSmsIntegration;
    @Mock
    private HistoryService mockHistoryService;

    private SmsService smsService;

    @BeforeEach
    void setUp() {
        smsService = new SmsService(mockSmsRepository, mockDefaultSettings, mockSmsIntegration, mockHistoryService);
    }

    @Test
    void saveSms_givenValidSmsRequest_thenSaveSms() {
        var request = createSmsRequest(null);

        smsService.saveSms(request);

        verify(mockSmsRepository, times(1)).save(any());
    }

    @Test
    void saveSms_givenSmsRequestWithoutSender_thenUseDefaultSettings() {
        var nullSender = createSmsRequest(sms -> sms.setSender(null));
        var blankSender = createSmsRequest(sms -> sms.setSender(" "));

        smsService.saveSms(nullSender);
        smsService.saveSms(blankSender);

        verify(mockDefaultSettings, times(2)).getSmsName();
        verify(mockSmsRepository, times(2)).save(any());
    }

    @Test
    void sendOldestPendingSms_whenNoPendingSms_thenNothingToSend() {
        when(mockSmsRepository.findByStatusEquals(any(), any())).thenReturn(List.of());

        smsService.sendOldestPendingMessages();

        verifyNoInteractions(mockSmsIntegration, mockHistoryService);
    }

    @Test
    void sendOldestPendingSms_whenSmsExceededMaxSendAttempts_thenMoveUndeliverableToHistory() {
        var smsEntities = List.of(createSms(message -> message.setSendingAttempts(3)));

        when(mockSmsRepository.findByStatusEquals(any(), any())).thenReturn(smsEntities);
        when(mockSmsIntegration.getMessageRetries()).thenReturn(3);

        smsService.sendOldestPendingMessages();

        verify(mockHistoryService, times(1)).createHistory(any(UndeliverableMessageDto.class));
        verify(mockSmsRepository, times(1)).deleteById(anyString());
    }

    @Test
    void sendOldestPendingSms_whenAttemptingToSendSmsAndFail_thenUpdateSendingAttempts() {
        var smsEntities = List.of(createSms(message -> message.setSendingAttempts(0)));
        var smsCaptor = ArgumentCaptor.forClass(SmsEntity.class);

        when(mockSmsRepository.findByStatusEquals(any(), any())).thenReturn(smsEntities);
        when(mockSmsIntegration.getMessageRetries()).thenReturn(3);
        when(mockSmsIntegration.sendSms(any())).thenThrow(new RestClientException("Connection refused"));

        smsService.sendOldestPendingMessages();

        verify(mockSmsRepository, times(1)).save(smsCaptor.capture());

        assertThat(smsCaptor.getValue().getSendingAttempts()).isEqualTo(1);
    }

    @Test
    void sendOldestPendingSms_whenSmsSentAndResponseStatus_OK_thenMoveToHistory() {
        var smsEntities = List.of(createSms(null));

        when(mockSmsRepository.findByStatusEquals(any(), any())).thenReturn(smsEntities);
        when(mockSmsIntegration.getMessageRetries()).thenReturn(3);
        when(mockSmsIntegration.sendSms(any())).thenReturn(ResponseEntity.ok(true));

        smsService.sendOldestPendingMessages();

        verify(mockHistoryService, times(1)).createHistory(any(SmsEntity.class));
        verify(mockSmsRepository, times(1)).deleteById(anyString());
    }

    private SmsEntity createSms(Consumer<SmsEntity> modifier) {
        var smsEntity = SmsEntity.builder()
            .withBatchId(UUID.randomUUID().toString())
            .withMessageId(UUID.randomUUID().toString())
            .withPartyId(PARTY_ID)
            .withExternalReferences(Map.of("key", "value"))
            .withMobileNumber(MOBILE_NUMBER)
            .withSender("Sundsvall")
            .withMessage("Message content")
            .withStatus(MessageStatus.PENDING)
            .withSendingAttempts(0)
            .withCreatedAt(LocalDateTime.now())
            .build();

        if (modifier != null) {
            modifier.accept(smsEntity);
        }

        return smsEntity;
    }

    private SmsRequest createSmsRequest(Consumer<SmsRequest> modifier) {
        var request = SmsRequest.builder()
            .withParty(Party.builder()
                .withPartyId(PARTY_ID)
                .withExternalReferences(List.of(
                    ExternalReference.builder()
                        .withKey("key")
                        .withValue("value")
                        .build()))
                .build())
            .withMobileNumber(MOBILE_NUMBER)
            .withSender("Sundsvall")
            .withMessage("Message content")
            .build();

        if (modifier != null) {
            modifier.accept(request);
        }

        return request;
    }
}
