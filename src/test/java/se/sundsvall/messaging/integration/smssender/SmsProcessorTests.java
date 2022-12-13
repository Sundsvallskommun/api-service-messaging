package se.sundsvall.messaging.integration.smssender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createSmsRequest;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import se.sundsvall.messaging.configuration.Defaults;
import se.sundsvall.messaging.configuration.RetryProperties;
import se.sundsvall.messaging.dto.SmsDto;
import se.sundsvall.messaging.integration.db.CounterRepository;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.WhitelistingService;
import se.sundsvall.messaging.service.event.IncomingSmsEvent;

import generated.se.sundsvall.smssender.SendSmsResponse;

@ExtendWith(MockitoExtension.class)
class SmsProcessorTests {

    private static final Gson GSON = new GsonBuilder().create();

    @Mock
    private RetryProperties mockRetryProperties;
    @Mock
    private MessageRepository mockMessageRepository;
    @Mock
    private HistoryRepository mockHistoryRepository;
    @Mock
    private CounterRepository mockCounterRepository;
    @Mock
    private WhitelistingService mockWhitelistingService;
    @Mock
    private SmsSenderIntegration mockSmsSenderIntegration;
    @Mock
    private Defaults mockDefaults;

    private SmsProcessor smsProcessor;

    @BeforeEach
    void setUp() {
        when(mockRetryProperties.getMaxAttempts()).thenReturn(3);
        when(mockRetryProperties.getInitialDelay()).thenReturn(Duration.ofMillis(1));
        when(mockRetryProperties.getMaxDelay()).thenReturn(Duration.ofMillis(100));

        smsProcessor = new SmsProcessor(mockRetryProperties, mockMessageRepository,
            mockHistoryRepository, mockCounterRepository, mockWhitelistingService,
            mockSmsSenderIntegration, mockDefaults);
    }

    @Test
    void testHandleIncomingSmsEvent_whenMessageIsNotFound() {
        when(mockMessageRepository.findByDeliveryId(any(String.class))).thenReturn(Optional.empty());

        smsProcessor.handleIncomingSmsEvent(new IncomingSmsEvent(this, "someMessageId"));

        verify(mockMessageRepository, times(1)).findByDeliveryId(any(String.class));
        verify(mockSmsSenderIntegration, never()).sendSms(any(SmsDto.class));
        verify(mockMessageRepository, never()).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, never()).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingSmsEvent() {
        var request = createSmsRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageAndDeliveryId)
                .withDeliveryId(messageAndDeliveryId)
                .withPartyId(request.getParty().getPartyId())
                .withType(MessageType.SMS)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(request))
                .build()));
        when(mockSmsSenderIntegration.sendSms(any(SmsDto.class)))
            .thenReturn(ResponseEntity.ok(new SendSmsResponse().sent(true)));
        when(mockWhitelistingService.isWhitelisted(any(MessageType.class), any(String.class))).thenReturn(true);

        smsProcessor.handleIncomingSmsEvent(new IncomingSmsEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockWhitelistingService, times(1)).isWhitelisted(any(MessageType.class), any(String.class));
        verify(mockSmsSenderIntegration, times(1)).sendSms(any(SmsDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingSmsEvent_whenSmsSenderIntegrationThrowsException() {
        var request = createSmsRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageAndDeliveryId)
                .withDeliveryId(messageAndDeliveryId)
                .withPartyId(request.getParty().getPartyId())
                .withType(MessageType.SMS)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(request))
                .build()));
        when(mockWhitelistingService.isWhitelisted(any(MessageType.class), any(String.class))).thenReturn(true);
        when(mockSmsSenderIntegration.sendSms(any(SmsDto.class))).thenThrow(RuntimeException.class);

        smsProcessor.handleIncomingSmsEvent(new IncomingSmsEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockWhitelistingService, times(1)).isWhitelisted(any(MessageType.class), any(String.class));
        verify(mockSmsSenderIntegration, times(3)).sendSms(any(SmsDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingSmsEvent_whenSmsSenderIntegrationReturnsOtherThanOk() {
        var request = createSmsRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageAndDeliveryId)
                .withDeliveryId(messageAndDeliveryId)
                .withPartyId(request.getParty().getPartyId())
                .withType(MessageType.SMS)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(request))
                .build()));
        when(mockWhitelistingService.isWhitelisted(any(MessageType.class), any(String.class))).thenReturn(true);
        when(mockSmsSenderIntegration.sendSms(any(SmsDto.class))).thenReturn(ResponseEntity.internalServerError().build());

        smsProcessor.handleIncomingSmsEvent(new IncomingSmsEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockWhitelistingService, times(1)).isWhitelisted(any(MessageType.class), any(String.class));
        verify(mockSmsSenderIntegration, times(3)).sendSms(any(SmsDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingSmsEvent_whenSmsSenderIntegrationReturnsOkButFalse() {
        var request = createSmsRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageAndDeliveryId)
                .withDeliveryId(messageAndDeliveryId)
                .withPartyId(request.getParty().getPartyId())
                .withType(MessageType.SMS)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(request))
                .build()));
        when(mockWhitelistingService.isWhitelisted(any(MessageType.class), any(String.class))).thenReturn(true);
        when(mockSmsSenderIntegration.sendSms(any(SmsDto.class)))
            .thenReturn(ResponseEntity.ok(new SendSmsResponse().sent(false)));

        smsProcessor.handleIncomingSmsEvent(new IncomingSmsEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockWhitelistingService, times(1)).isWhitelisted(any(MessageType.class), any(String.class));
        verify(mockSmsSenderIntegration, times(3)).sendSms(any(SmsDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingSmsEvent_whenWhitelistingServiceReturnsFalse() {
        var request = createSmsRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageAndDeliveryId)
                .withDeliveryId(messageAndDeliveryId)
                .withPartyId(request.getParty().getPartyId())
                .withType(MessageType.SMS)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(request))
                .build()));
        when(mockWhitelistingService.isWhitelisted(any(MessageType.class), any(String.class))).thenReturn(false);

        smsProcessor.handleIncomingSmsEvent(new IncomingSmsEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockWhitelistingService, times(1)).isWhitelisted(any(MessageType.class), any(String.class));
        verify(mockSmsSenderIntegration, never()).sendSms(any(SmsDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void test_mapToDto() {
        var request = createSmsRequest();

        var message = MessageEntity.builder()
            .withMessageId(UUID.randomUUID().toString())
            .withPartyId(request.getParty().getPartyId())
            .withType(MessageType.SMS)
            .withStatus(MessageStatus.PENDING)
            .withContent(GSON.toJson(request))
            .build();

        var dto = smsProcessor.mapToDto(message);

        assertThat(dto.getSender()).isEqualTo(request.getSender());
        assertThat(dto.getMobileNumber()).isEqualTo(request.getMobileNumber());
        assertThat(dto.getMessage()).isEqualTo(request.getMessage());
    }
}
