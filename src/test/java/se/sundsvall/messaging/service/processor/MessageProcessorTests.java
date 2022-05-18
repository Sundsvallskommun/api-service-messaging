package se.sundsvall.messaging.service.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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

    @Mock
    private ApplicationEventPublisher mockEventPublisher;
    @Mock
    private MessageRepository mockMessageRepository;
    @Mock
    private HistoryRepository mockHistoryRepository;
    @Mock
    private FeedbackSettingsIntegration mockFeedbackSettingsIntegration;

    @Mock
    private FeedbackChannelDto mockFeedbackChannel;

    private MessageProcessor messageProcessor;

    @BeforeEach
    void setUp() {
        messageProcessor = new MessageProcessor(mockEventPublisher, mockMessageRepository,
            mockHistoryRepository, mockFeedbackSettingsIntegration);
    }

    @Test
    void testHandleIncomingMessageEvent_whenMessageIsNotFound() {
        when(mockMessageRepository.findById(any(String.class))).thenReturn(Optional.empty());

        messageProcessor.handleIncomingMessageEvent(new IncomingMessageEvent(this, "someMessageId"));

        verify(mockMessageRepository, times(1)).findById(any(String.class));
        verify(mockFeedbackSettingsIntegration, never()).getSettingsByPartyId(any(String.class));
        verify(mockMessageRepository, never()).deleteById(any(String.class));
        verify(mockHistoryRepository, never()).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingMessageEvent_whenNoFeedbackSettingsExist() {
        when(mockMessageRepository.findById(any(String.class))).thenReturn(Optional.of(MessageEntity.builder()
            .withMessageId("someMessageId")
            .withPartyId("somePartyId")
            .withType(MessageType.EMAIL)
            .build()));

        when(mockFeedbackSettingsIntegration.getSettingsByPartyId(any(String.class)))
            .thenReturn(List.of());

        messageProcessor.handleIncomingMessageEvent(new IncomingMessageEvent(this, "someMessageId"));

        verify(mockMessageRepository, times(1)).findById(any(String.class));
        verify(mockFeedbackSettingsIntegration, times(1)).getSettingsByPartyId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingMessageEvent_whenFeedbackSettingsHasNoContactMethod() {
        when(mockMessageRepository.findById(any(String.class))).thenReturn(Optional.of(MessageEntity.builder()
            .withMessageId("someMessageId")
            .withPartyId("somePartyId")
            .withType(MessageType.EMAIL)
            .build()));

        when(mockFeedbackSettingsIntegration.getSettingsByPartyId(any(String.class)))
            .thenReturn(List.of(mockFeedbackChannel));
        when(mockFeedbackChannel.getContactMethod()).thenReturn(null);

        messageProcessor.handleIncomingMessageEvent(new IncomingMessageEvent(this, "someMessageId"));

        verify(mockMessageRepository, times(1)).findById(any(String.class));
        verify(mockFeedbackSettingsIntegration, times(1)).getSettingsByPartyId(any(String.class));
        verify(mockMessageRepository, never()).save(any(MessageEntity.class));
        verify(mockMessageRepository, times(1)).deleteById(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingMessageEvent_whenFeedbackSettingsIndicateNoContactIsWanted() {
        when(mockMessageRepository.findById(any(String.class))).thenReturn(Optional.of(MessageEntity.builder()
            .withMessageId("someMessageId")
            .withPartyId("somePartyId")
            .withType(MessageType.EMAIL)
            .build()));

        when(mockFeedbackSettingsIntegration.getSettingsByPartyId(any(String.class)))
            .thenReturn(List.of(mockFeedbackChannel));
        when(mockFeedbackChannel.getContactMethod()).thenReturn(ContactMethod.EMAIL);
        when(mockFeedbackChannel.isFeedbackWanted()).thenReturn(false);

        messageProcessor.handleIncomingMessageEvent(new IncomingMessageEvent(this, "someMessageId"));

        verify(mockMessageRepository, times(1)).findById(any(String.class));
        verify(mockFeedbackSettingsIntegration, times(1)).getSettingsByPartyId(any(String.class));
        verify(mockMessageRepository, never()).save(any(MessageEntity.class));
        verify(mockMessageRepository, times(1)).deleteById(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingMessageEvent_whenFeedbackSettingsIndicateEmailAsContactMethod() {
        when(mockMessageRepository.findById(any(String.class))).thenReturn(Optional.of(MessageEntity.builder()
            .withMessageId("someMessageId")
            .withPartyId("somePartyId")
            .withType(MessageType.EMAIL)
            .build()));

        when(mockFeedbackSettingsIntegration.getSettingsByPartyId(any(String.class)))
            .thenReturn(List.of(mockFeedbackChannel));
        when(mockFeedbackChannel.getContactMethod()).thenReturn(ContactMethod.EMAIL);
        when(mockFeedbackChannel.isFeedbackWanted()).thenReturn(true);

        messageProcessor.handleIncomingMessageEvent(new IncomingMessageEvent(this, "someMessageId"));

        verify(mockMessageRepository, times(1)).findById(any(String.class));
        verify(mockFeedbackSettingsIntegration, times(1)).getSettingsByPartyId(any(String.class));
        verify(mockMessageRepository, times(1)).save(any(MessageEntity.class));
        verify(mockHistoryRepository, never()).save(any(HistoryEntity.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingEmailEvent.class));
    }

    @Test
    void testHandleIncomingMessageEvent_whenFeedbackSettingsIndicateSmsAsContactMethod() {
        when(mockMessageRepository.findById(any(String.class))).thenReturn(Optional.of(MessageEntity.builder()
            .withMessageId("someMessageId")
            .withPartyId("somePartyId")
            .withType(MessageType.EMAIL)
            .build()));

        when(mockFeedbackSettingsIntegration.getSettingsByPartyId(any(String.class)))
                .thenReturn(List.of(mockFeedbackChannel));
        when(mockFeedbackChannel.getContactMethod()).thenReturn(ContactMethod.SMS);
        when(mockFeedbackChannel.isFeedbackWanted()).thenReturn(true);

        messageProcessor.handleIncomingMessageEvent(new IncomingMessageEvent(this, "someMessageId"));

        verify(mockMessageRepository, times(1)).findById(any(String.class));
        verify(mockFeedbackSettingsIntegration, times(1)).getSettingsByPartyId(any(String.class));
        verify(mockMessageRepository, times(1)).save(any(MessageEntity.class));
        verify(mockHistoryRepository, never()).save(any(HistoryEntity.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingSmsEvent.class));
    }
}
