package se.sundsvall.messaging.integration.digitalmailsender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createDigitalMailRequest;

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

import se.sundsvall.messaging.dto.DigitalMailDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.ContentType;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.event.IncomingDigitalMailEvent;

@ExtendWith(MockitoExtension.class)
class DigitalMailProcessorTests {

    private static final Gson GSON = new GsonBuilder().create();

    @Mock
    private MessageRepository mockMessageRepository;
    @Mock
    private HistoryRepository mockHistoryRepository;
    @Mock
    private DigitalMailSenderIntegration mockDigitalMailSenderIntegration;

    private DigitalMailProcessor digitalMailProcessor;

    @BeforeEach
    void setUp() {
        var backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(10);
        backOffPolicy.setMultiplier(2);

        var retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(5));
        retryTemplate.setBackOffPolicy(backOffPolicy);

        digitalMailProcessor = new DigitalMailProcessor(retryTemplate, mockMessageRepository,
            mockHistoryRepository, mockDigitalMailSenderIntegration);
    }

    @Test
    void testHandleIncomingDigitalMailEvent_whenMessageIsNotFound() {
        when(mockMessageRepository.findById(any(String.class))).thenReturn(Optional.empty());

        digitalMailProcessor.handleIncomingDigitalMailEvent(new IncomingDigitalMailEvent(this, "someMessageId"));

        verify(mockMessageRepository, times(1)).findById(any(String.class));
        verify(mockDigitalMailSenderIntegration, never()).sendDigitalMail(any(DigitalMailDto.class));
        verify(mockMessageRepository, never()).deleteById(any(String.class));
        verify(mockHistoryRepository, never()).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingDigitalMailEvent() {
        var digitalMailRequest = createDigitalMailRequest();
        var messageId = UUID.randomUUID().toString();

        when(mockMessageRepository.findById(eq(messageId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageId)
                .withPartyId(digitalMailRequest.getParty().getPartyId())
                .withType(MessageType.DIGITAL_MAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(digitalMailRequest))
                .build()));
        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class))).thenReturn(ResponseEntity.ok(true));

        digitalMailProcessor.handleIncomingDigitalMailEvent(new IncomingDigitalMailEvent(this, messageId));

        verify(mockMessageRepository, times(1)).findById(eq(messageId));
        verify(mockDigitalMailSenderIntegration, times(1)).sendDigitalMail(any(DigitalMailDto.class));
        verify(mockMessageRepository, times(1)).deleteById(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingDigitalMailEvent_whenDigitalMailSenderIntegrationThrowsException() {
        var digitalMailRequest = createDigitalMailRequest();
        var messageId = UUID.randomUUID().toString();

        when(mockMessageRepository.findById(eq(messageId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageId)
                .withPartyId(digitalMailRequest.getParty().getPartyId())
                .withType(MessageType.DIGITAL_MAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(digitalMailRequest))
                .build()));
        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class))).thenThrow(RuntimeException.class);

        digitalMailProcessor.handleIncomingDigitalMailEvent(new IncomingDigitalMailEvent(this, messageId));

        verify(mockMessageRepository, times(1)).findById(eq(messageId));
        verify(mockDigitalMailSenderIntegration, times(5)).sendDigitalMail(any(DigitalMailDto.class));
        verify(mockMessageRepository, times(1)).deleteById(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingDigitalMailEvent_whenDigitalMailSenderIntegrationReturnsOtherThanOk() {
        var digitalMailRequest = createDigitalMailRequest();
        var messageId = UUID.randomUUID().toString();

        when(mockMessageRepository.findById(eq(messageId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageId)
                .withPartyId(digitalMailRequest.getParty().getPartyId())
                .withType(MessageType.DIGITAL_MAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(digitalMailRequest))
                .build()));
        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class)))
            .thenReturn(ResponseEntity.internalServerError().build());

        digitalMailProcessor.handleIncomingDigitalMailEvent(new IncomingDigitalMailEvent(this, messageId));

        verify(mockMessageRepository, times(1)).findById(eq(messageId));
        verify(mockDigitalMailSenderIntegration, times(5)).sendDigitalMail(any(DigitalMailDto.class));
        verify(mockMessageRepository, times(1)).deleteById(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingDigitalMailEvent_whenDigitalMailSenderIntegrationReturnsOkButFalse() {
        var digitalMailRequest = createDigitalMailRequest();
        var messageId = UUID.randomUUID().toString();

        when(mockMessageRepository.findById(eq(messageId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageId)
                .withPartyId(digitalMailRequest.getParty().getPartyId())
                .withType(MessageType.DIGITAL_MAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(digitalMailRequest))
                .build()));
        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class))).thenReturn(ResponseEntity.ok(false));

        digitalMailProcessor.handleIncomingDigitalMailEvent(new IncomingDigitalMailEvent(this, messageId));

        verify(mockMessageRepository, times(1)).findById(eq(messageId));
        verify(mockDigitalMailSenderIntegration, times(5)).sendDigitalMail(any(DigitalMailDto.class));
        verify(mockMessageRepository, times(1)).deleteById(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void test_mapToDto() {
        var digitalMailRequest = createDigitalMailRequest();

        var message = MessageEntity.builder()
            .withMessageId(UUID.randomUUID().toString())
            .withPartyId(digitalMailRequest.getParty().getPartyId())
            .withType(MessageType.DIGITAL_MAIL)
            .withStatus(MessageStatus.PENDING)
            .withContent(GSON.toJson(digitalMailRequest))
            .build();

        var dto = digitalMailProcessor.mapToDto(message);

        assertThat(dto.getSubject()).isEqualTo(digitalMailRequest.getSubject());
        assertThat(dto.getPartyId()).isEqualTo(digitalMailRequest.getParty().getPartyId());
        assertThat(dto.getContentType()).isEqualTo(ContentType.fromString(digitalMailRequest.getContentType()));
        assertThat(dto.getBody()).isEqualTo(digitalMailRequest.getBody());
        assertThat(dto.getAttachments()).hasSameSizeAs(digitalMailRequest.getAttachments());
    }
}
