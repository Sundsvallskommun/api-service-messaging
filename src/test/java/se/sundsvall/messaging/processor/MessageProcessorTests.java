package se.sundsvall.messaging.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.configuration.Defaults;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.feedbacksettings.FeedbackSettingsIntegration;
import se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod;
import se.sundsvall.messaging.integration.feedbacksettings.model.FeedbackChannelDto;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.event.IncomingEmailEvent;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.service.event.IncomingSmsEvent;

@ExtendWith(MockitoExtension.class)
class MessageProcessorTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ApplicationEventPublisher mockEventPublisher;
    @Mock
    private MessageRepository mockMessageRepository;
    @Mock
    private HistoryRepository mockHistoryRepository;
    @Mock
    private Defaults mockDefaults;
    @Mock
    private FeedbackSettingsIntegration mockFeedbackSettingsIntegration;

    @Mock
    private FeedbackChannelDto mockFeedbackChannel;

    private MessageProcessor messageProcessor;

    @BeforeEach
    void setUp() {
        messageProcessor = new MessageProcessor(mockEventPublisher, mockMessageRepository,
            mockHistoryRepository, mockDefaults, mockFeedbackSettingsIntegration);
    }

    @Test
    void testHandleIncomingMessageEvent_whenMessageIsNotFound() {
        when(mockMessageRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        messageProcessor.handleIncomingMessageEvent(new IncomingMessageEvent(this, 12345L));

        verify(mockMessageRepository, times(1)).findById(any(Long.class));
        verify(mockFeedbackSettingsIntegration, never()).getSettingsByPartyId(ArgumentMatchers.any(), any(String.class));
        verify(mockMessageRepository, never()).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, never()).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingMessageEvent_whenNoFeedbackSettingsExist() throws JsonProcessingException {
        when(mockMessageRepository.findById(any(Long.class))).thenReturn(Optional.of(MessageEntity.builder()
            .withMessageId("someMessageId")
            .withPartyId("somePartyId")
            .withType(MessageType.EMAIL)
            .withContent(objectMapper.writeValueAsString(EmailRequest.builder()
                .withHeaders(List.of())
                .build()))
            .build()));

        when(mockFeedbackSettingsIntegration.getSettingsByPartyId(any(), any(String.class)))
            .thenReturn(List.of());

        messageProcessor.handleIncomingMessageEvent(new IncomingMessageEvent(this, 12345L));

        verify(mockMessageRepository, times(1)).findById(any(Long.class));
        verify(mockFeedbackSettingsIntegration, times(1)).getSettingsByPartyId(any(), any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingMessageEvent_whenFeedbackSettingsHasNoContactMethod() throws JsonProcessingException {
        when(mockMessageRepository.findById(any(Long.class))).thenReturn(Optional.of(MessageEntity.builder()
            .withMessageId("someMessageId")
            .withPartyId("somePartyId")
            .withType(MessageType.EMAIL)
            .withContent(objectMapper.writeValueAsString(EmailRequest.builder()
                .withHeaders(List.of())
                .build()))
            .build()));

        when(mockFeedbackSettingsIntegration.getSettingsByPartyId(any(), any(String.class)))
            .thenReturn(List.of(mockFeedbackChannel));
        when(mockFeedbackChannel.getContactMethod()).thenReturn(null);

        messageProcessor.handleIncomingMessageEvent(new IncomingMessageEvent(this, 12345L));

        verify(mockMessageRepository, times(1)).findById(any(Long.class));
        verify(mockFeedbackSettingsIntegration, times(1)).getSettingsByPartyId(any(), any(String.class));
        verify(mockMessageRepository, never()).save(any(MessageEntity.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingMessageEvent_whenFeedbackSettingsIndicateNoContactIsWanted() throws JsonProcessingException {
        when(mockMessageRepository.findById(any(Long.class))).thenReturn(Optional.of(MessageEntity.builder()
            .withMessageId("someMessageId")
            .withPartyId("somePartyId")
            .withType(MessageType.EMAIL)
            .withContent(objectMapper.writeValueAsString(EmailRequest.builder()
                .withHeaders(List.of())
                .build()))
            .build()));

        when(mockFeedbackSettingsIntegration.getSettingsByPartyId(any(), any(String.class)))
            .thenReturn(List.of(mockFeedbackChannel));
        when(mockFeedbackChannel.getContactMethod()).thenReturn(ContactMethod.EMAIL);
        when(mockFeedbackChannel.isFeedbackWanted()).thenReturn(false);

        messageProcessor.handleIncomingMessageEvent(new IncomingMessageEvent(this, 12345L));

        verify(mockMessageRepository, times(1)).findById(any(Long.class));
        verify(mockFeedbackSettingsIntegration, times(1)).getSettingsByPartyId(any(), any(String.class));
        verify(mockMessageRepository, never()).save(any(MessageEntity.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingMessageEvent_whenFeedbackSettingsIndicateEmailAsContactMethod() throws JsonProcessingException {
        when(mockMessageRepository.findById(any(Long.class))).thenReturn(Optional.of(MessageEntity.builder()
            .withMessageId("someMessageId")
            .withPartyId("somePartyId")
            .withType(MessageType.EMAIL)
            .withContent(objectMapper.writeValueAsString(EmailRequest.builder()
                .withHeaders(List.of())
                .build()))
            .build()));

        when(mockFeedbackSettingsIntegration.getSettingsByPartyId(any(), any(String.class)))
            .thenReturn(List.of(mockFeedbackChannel));
        when(mockFeedbackChannel.getContactMethod()).thenReturn(ContactMethod.EMAIL);
        when(mockFeedbackChannel.isFeedbackWanted()).thenReturn(true);

        messageProcessor.handleIncomingMessageEvent(new IncomingMessageEvent(this, 12345L));

        verify(mockMessageRepository, times(1)).findById(any(Long.class));
        verify(mockFeedbackSettingsIntegration, times(1)).getSettingsByPartyId(any(), any(String.class));
        verify(mockMessageRepository, times(1)).save(any(MessageEntity.class));
        verify(mockHistoryRepository, never()).save(any(HistoryEntity.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingEmailEvent.class));
    }

    @Test
    void testHandleIncomingMessageEvent_whenFeedbackSettingsIndicateSmsAsContactMethod() {
        when(mockMessageRepository.findById(any(Long.class))).thenReturn(Optional.of(MessageEntity.builder()
            .withMessageId("someMessageId")
            .withPartyId("somePartyId")
            .withType(MessageType.EMAIL)
            .withContent("{}")
            .build()));

        when(mockFeedbackSettingsIntegration.getSettingsByPartyId(any(), any(String.class)))
                .thenReturn(List.of(mockFeedbackChannel));
        when(mockFeedbackChannel.getContactMethod()).thenReturn(ContactMethod.SMS);
        when(mockFeedbackChannel.isFeedbackWanted()).thenReturn(true);

        messageProcessor.handleIncomingMessageEvent(new IncomingMessageEvent(this, 12345L));

        verify(mockMessageRepository, times(1)).findById(any(Long.class));
        verify(mockFeedbackSettingsIntegration, times(1)).getSettingsByPartyId(any(), any(String.class));
        verify(mockMessageRepository, times(1)).save(any(MessageEntity.class));
        verify(mockHistoryRepository, never()).save(any(HistoryEntity.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingSmsEvent.class));
    }
}
