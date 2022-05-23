package se.sundsvall.messaging.integration.smssender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createEmailRequest;
import static se.sundsvall.messaging.TestDataFactory.createSmsRequest;

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
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import se.sundsvall.messaging.dto.SmsDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.event.IncomingSmsEvent;

@ExtendWith(MockitoExtension.class)
class SmsProcessorTests {

    private static final Gson GSON = new GsonBuilder().create();

    @Mock
    private MessageRepository mockMessageRepository;
    @Mock
    private HistoryRepository mockHistoryRepository;
    @Mock
    private SmsSenderIntegration mockSmsSenderIntegration;

    private SmsProcessor smsProcessor;

    @BeforeEach
    void setUp() {
        var backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(10);
        backOffPolicy.setMultiplier(2);

        var retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(5));
        retryTemplate.setBackOffPolicy(backOffPolicy);

        smsProcessor = new SmsProcessor(retryTemplate, mockMessageRepository,
            mockHistoryRepository, mockSmsSenderIntegration);
    }

    @Test
    void testHandleIncomingSmsEvent_whenMessageIsNotFound() {
        when(mockMessageRepository.findById(any(String.class))).thenReturn(Optional.empty());

        smsProcessor.handleIncomingSmsEvent(new IncomingSmsEvent(this, "someMessageId"));

        verify(mockMessageRepository, times(1)).findById(any(String.class));
        verify(mockSmsSenderIntegration, never()).sendSms(any(SmsDto.class));
        verify(mockMessageRepository, never()).deleteById(any(String.class));
        verify(mockHistoryRepository, never()).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingSmsEvent() {
        var emailRequest = createEmailRequest();
        var messageId = UUID.randomUUID().toString();

        when(mockMessageRepository.findById(eq(messageId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageId)
                .withPartyId(emailRequest.getParty().getPartyId())
                .withType(MessageType.EMAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(emailRequest))
                .build()));
        when(mockSmsSenderIntegration.sendSms(any(SmsDto.class))).thenReturn(ResponseEntity.ok(true));

        smsProcessor.handleIncomingSmsEvent(new IncomingSmsEvent(this, messageId));

        verify(mockMessageRepository, times(1)).findById(eq(messageId));
        verify(mockSmsSenderIntegration, times(1)).sendSms(any(SmsDto.class));
        verify(mockMessageRepository, times(1)).deleteById(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingSmsEvent_whenSmsSenderIntegrationThrowsException() {
        var emailRequest = createEmailRequest();
        var messageId = UUID.randomUUID().toString();

        when(mockMessageRepository.findById(eq(messageId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageId)
                .withPartyId(emailRequest.getParty().getPartyId())
                .withType(MessageType.EMAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(emailRequest))
                .build()));
        when(mockSmsSenderIntegration.sendSms(any(SmsDto.class))).thenThrow(RuntimeException.class);

        smsProcessor.handleIncomingSmsEvent(new IncomingSmsEvent(this, messageId));

        verify(mockMessageRepository, times(1)).findById(eq(messageId));
        verify(mockSmsSenderIntegration, times(5)).sendSms(any(SmsDto.class));
        verify(mockMessageRepository, times(1)).deleteById(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingSmsEvent_whenSmsSenderIntegrationReturnsOtherThanOk() {
        var emailRequest = createEmailRequest();
        var messageId = UUID.randomUUID().toString();

        when(mockMessageRepository.findById(eq(messageId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageId)
                .withPartyId(emailRequest.getParty().getPartyId())
                .withType(MessageType.EMAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(emailRequest))
                .build()));
        when(mockSmsSenderIntegration.sendSms(any(SmsDto.class))).thenReturn(ResponseEntity.internalServerError().build());

        smsProcessor.handleIncomingSmsEvent(new IncomingSmsEvent(this, messageId));

        verify(mockMessageRepository, times(1)).findById(eq(messageId));
        verify(mockSmsSenderIntegration, times(5)).sendSms(any(SmsDto.class));
        verify(mockMessageRepository, times(1)).deleteById(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingSmsEvent_whenSmsSenderIntegrationReturnsOkButFalse() {
        var emailRequest = createEmailRequest();
        var messageId = UUID.randomUUID().toString();

        when(mockMessageRepository.findById(eq(messageId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageId)
                .withPartyId(emailRequest.getParty().getPartyId())
                .withType(MessageType.EMAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(emailRequest))
                .build()));
        when(mockSmsSenderIntegration.sendSms(any(SmsDto.class))).thenReturn(ResponseEntity.ok(false));

        smsProcessor.handleIncomingSmsEvent(new IncomingSmsEvent(this, messageId));

        verify(mockMessageRepository, times(1)).findById(eq(messageId));
        verify(mockSmsSenderIntegration, times(5)).sendSms(any(SmsDto.class));
        verify(mockMessageRepository, times(1)).deleteById(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void test_mapToDto() {
        var smsRequest = createSmsRequest();

        var message = MessageEntity.builder()
            .withMessageId(UUID.randomUUID().toString())
            .withPartyId(smsRequest.getParty().getPartyId())
            .withType(MessageType.SMS)
            .withStatus(MessageStatus.PENDING)
            .withContent(GSON.toJson(smsRequest))
            .build();

        var dto = smsProcessor.mapToDto(message);

        assertThat(dto.getSender()).isEqualTo(smsRequest.getSender());
        assertThat(dto.getMobileNumber()).isEqualTo(smsRequest.getMobileNumber());
        assertThat(dto.getMessage()).isEqualTo(smsRequest.getMessage());
    }
}
