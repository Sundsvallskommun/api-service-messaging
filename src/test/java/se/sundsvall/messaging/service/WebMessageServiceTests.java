package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
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

import se.sundsvall.messaging.api.model.WebMessageRequest;
import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.dto.UndeliverableMessageDto;
import se.sundsvall.messaging.dto.WebMessageDto;
import se.sundsvall.messaging.integration.db.WebMessageRepository;
import se.sundsvall.messaging.integration.db.entity.WebMessageEntity;
import se.sundsvall.messaging.integration.webmessagesender.WebMessageSenderIntegration;
import se.sundsvall.messaging.integration.webmessagesender.WebMessageSenderIntegrationProperties;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.Party;

@ExtendWith(MockitoExtension.class)
class WebMessageServiceTests {
    
    private static final String PARTY_ID = UUID.randomUUID().toString();
    private static final String MOBILE_NUMBER = "+46701234567";

    @Mock
    private WebMessageRepository mockWebMessageRepository;
    @Mock
    private DefaultSettings mockDefaultSettings;
    @Mock
    private WebMessageSenderIntegrationProperties mockWebMessageSenderIntegrationProperties;
    @Mock
    private WebMessageSenderIntegration mockWebMessageSenderIntegration;
    @Mock
    private HistoryService mockHistoryService;

    private WebMessageService webMessageService;

    @BeforeEach
    void setUp() {
        webMessageService = new WebMessageService(mockWebMessageRepository,
            mockWebMessageSenderIntegrationProperties, mockWebMessageSenderIntegration,
            mockHistoryService);
    }

    @Test
    void test_getPollDelay() {
        var pollDelay = Duration.ofSeconds(12);

        when(mockWebMessageSenderIntegrationProperties.getPollDelay()).thenReturn(pollDelay);

        assertThat(webMessageService.getPollDelay()).isEqualTo(pollDelay);

        verify(mockWebMessageSenderIntegrationProperties, times(1)).getPollDelay();
    }

    @Test
    void test_run() {
        assertThatNoException().isThrownBy(() -> webMessageService.run());
    }

    @Test
    void saveWebMessage_givenValidWebMessageRequest_thenSaveWebMessage() {
        var request = createWebMessageRequest();

        webMessageService.saveWebMessage(request);

        verify(mockWebMessageRepository, times(1)).save(any());
    }

    @Test
    void sendOldestPendingWebMessage_whenNoPendingWebMessage_thenNothingToSend() {
        when(mockWebMessageRepository.findLatestWithStatus(any(MessageStatus.class))).thenReturn(List.of());

        webMessageService.sendOldestPendingMessages();

        verifyNoInteractions(mockWebMessageSenderIntegration, mockHistoryService);
    }

    @Test
    void sendOldestPendingWebMessage_whenWebMessageExceededMaxSendAttempts_thenMoveUndeliverableToHistory() {
        var webMessageEntities = List.of(createWebMessageEntity(message -> message.setSendingAttempts(3)));

        when(mockWebMessageRepository.findLatestWithStatus(any(MessageStatus.class))).thenReturn(webMessageEntities);
        when(mockWebMessageSenderIntegrationProperties.getMaxRetries()).thenReturn(3);

        webMessageService.sendOldestPendingMessages();

        verify(mockHistoryService, times(1)).createHistory(any(UndeliverableMessageDto.class));
        verify(mockWebMessageRepository, times(1)).deleteById(anyString());
    }

    @Test
    void sendOldestPendingSms_whenAttemptingToSendSmsAndFail_thenUpdateSendingAttempts() {
        var smsEntities = List.of(createWebMessageEntity(message -> message.setSendingAttempts(0)));
        var webMessageCaptor = ArgumentCaptor.forClass(WebMessageEntity.class);

        when(mockWebMessageRepository.findLatestWithStatus(any(MessageStatus.class))).thenReturn(smsEntities);
        when(mockWebMessageSenderIntegrationProperties.getMaxRetries()).thenReturn(3);
        when(mockWebMessageSenderIntegration.sendWebMessage(any(WebMessageDto.class)))
            .thenThrow(new RestClientException("Connection refused"));

        webMessageService.sendOldestPendingMessages();

        verify(mockWebMessageRepository, times(1)).save(webMessageCaptor.capture());

        assertThat(webMessageCaptor.getValue().getSendingAttempts()).isEqualTo(1);
    }

    @Test
    void sendOldestPendingSms_whenSmsSentAndResponseStatus_OK_thenMoveToHistory() {
        var smsEntities = List.of(createWebMessageEntity());

        when(mockWebMessageRepository.findLatestWithStatus(any(MessageStatus.class))).thenReturn(smsEntities);
        when(mockWebMessageSenderIntegrationProperties.getMaxRetries()).thenReturn(3);
        when(mockWebMessageSenderIntegration.sendWebMessage(any(WebMessageDto.class)))
            .thenReturn(ResponseEntity.created(null).build());

        webMessageService.sendOldestPendingMessages();

        verify(mockHistoryService, times(1)).createHistory(any(WebMessageEntity.class));
        verify(mockWebMessageRepository, times(1)).deleteById(anyString());
    }

    private WebMessageEntity createWebMessageEntity() {
        return createWebMessageEntity(null);
    }

    private WebMessageEntity createWebMessageEntity(final Consumer<WebMessageEntity> modifier) {
        var webMessageEntity = WebMessageEntity.builder()
            .withBatchId(UUID.randomUUID().toString())
            .withMessageId(UUID.randomUUID().toString())
            .withPartyId(PARTY_ID)
            .withExternalReferences(Map.of("key", "value"))
            .withMessage("Message content")
            .withStatus(MessageStatus.PENDING)
            .withSendingAttempts(0)
            .withCreatedAt(LocalDateTime.now())
            .build();

        if (modifier != null) {
            modifier.accept(webMessageEntity);
        }

        return webMessageEntity;
    }

    private WebMessageRequest createWebMessageRequest() {
        return createWebMessageRequest(null);
    }

    private WebMessageRequest createWebMessageRequest(final Consumer<WebMessageRequest> modifier) {
        var request = WebMessageRequest.builder()
            .withMessage("someMessage")
            .withParty(Party.builder()
                .withPartyId(PARTY_ID)
                .withExternalReferences(List.of(
                    ExternalReference.builder()
                        .withKey("key")
                        .withValue("value")
                        .build()))
                .build())
            .build();

        if (modifier != null) {
            modifier.accept(request);
        }

        return request;
    }
}
