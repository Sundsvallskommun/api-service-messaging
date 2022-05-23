package se.sundsvall.messaging.integration.webmessagesender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createEmailRequest;
import static se.sundsvall.messaging.TestDataFactory.createWebMessageRequest;

import java.net.URI;
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

import se.sundsvall.messaging.dto.WebMessageDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.event.IncomingWebMessageEvent;

@ExtendWith(MockitoExtension.class)
class WebMessageProcessorTests {

    private static final Gson GSON = new GsonBuilder().create();

    @Mock
    private MessageRepository mockMessageRepository;
    @Mock
    private HistoryRepository mockHistoryRepository;
    @Mock
    private WebMessageSenderIntegration mockWebMessageSenderIntegration;

    private WebMessageProcessor webMessageProcessor;

    @BeforeEach
    void setUp() {
        var backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(10);
        backOffPolicy.setMultiplier(2);

        var retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(5));
        retryTemplate.setBackOffPolicy(backOffPolicy);

        webMessageProcessor = new WebMessageProcessor(retryTemplate, mockMessageRepository,
            mockHistoryRepository, mockWebMessageSenderIntegration);
    }

    @Test
    void testHandleIncomingWebMessageEvent_whenMessageIsNotFound() {
        when(mockMessageRepository.findById(any(String.class))).thenReturn(Optional.empty());

        webMessageProcessor.handleIncomingWebMessageEvent(new IncomingWebMessageEvent(this, "someMessageId"));

        verify(mockMessageRepository, times(1)).findById(any(String.class));
        verify(mockWebMessageSenderIntegration, never()).sendWebMessage(any(WebMessageDto.class));
        verify(mockMessageRepository, never()).deleteById(any(String.class));
        verify(mockHistoryRepository, never()).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingWebMessageEvent() {
        var webMessageRequest = createWebMessageRequest();
        var messageId = UUID.randomUUID().toString();

        when(mockMessageRepository.findById(eq(messageId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageId)
                .withPartyId(webMessageRequest.getParty().getPartyId())
                .withType(MessageType.WEB_MESSAGE)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(webMessageRequest))
                .build()));
        when(mockWebMessageSenderIntegration.sendWebMessage(any(WebMessageDto.class)))
            .thenReturn(ResponseEntity.created(URI.create("some-uri")).build());

        webMessageProcessor.handleIncomingWebMessageEvent(new IncomingWebMessageEvent(this, messageId));

        verify(mockMessageRepository, times(1)).findById(eq(messageId));
        verify(mockWebMessageSenderIntegration, times(1)).sendWebMessage(any(WebMessageDto.class));
        verify(mockMessageRepository, times(1)).deleteById(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingWebMessageEvent_whenWebMessageSenderIntegrationThrowsException() {
        var webMessageRequest = createWebMessageRequest();
        var messageId = UUID.randomUUID().toString();

        when(mockMessageRepository.findById(eq(messageId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageId)
                .withPartyId(webMessageRequest.getParty().getPartyId())
                .withType(MessageType.WEB_MESSAGE)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(webMessageRequest))
                .build()));
        when(mockWebMessageSenderIntegration.sendWebMessage(any(WebMessageDto.class))).thenThrow(RuntimeException.class);

        webMessageProcessor.handleIncomingWebMessageEvent(new IncomingWebMessageEvent(this, messageId));

        verify(mockMessageRepository, times(1)).findById(eq(messageId));
        verify(mockWebMessageSenderIntegration, times(5)).sendWebMessage(any(WebMessageDto.class));
        verify(mockMessageRepository, times(1)).deleteById(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingWebMessageEvent_whenWebMessageSenderIntegrationReturnsOtherThanOk() {
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
        when(mockWebMessageSenderIntegration.sendWebMessage(any(WebMessageDto.class)))
            .thenReturn(ResponseEntity.internalServerError().build());

        webMessageProcessor.handleIncomingWebMessageEvent(new IncomingWebMessageEvent(this, messageId));

        verify(mockMessageRepository, times(1)).findById(eq(messageId));
        verify(mockWebMessageSenderIntegration, times(5)).sendWebMessage(any(WebMessageDto.class));
        verify(mockMessageRepository, times(1)).deleteById(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void test_mapToDto() {
        var webMessageRequest = createWebMessageRequest();

        var message = MessageEntity.builder()
            .withMessageId(UUID.randomUUID().toString())
            .withPartyId(webMessageRequest.getParty().getPartyId())
            .withType(MessageType.WEB_MESSAGE)
            .withStatus(MessageStatus.PENDING)
            .withContent(GSON.toJson(webMessageRequest))
            .build();

        var dto = webMessageProcessor.mapToDto(message);

        assertThat(dto.getParty()).isEqualTo(webMessageRequest.getParty());
        assertThat(dto.getMessage()).isEqualTo(webMessageRequest.getMessage());
    }
}
