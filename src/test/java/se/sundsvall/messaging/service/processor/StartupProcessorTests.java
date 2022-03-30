package se.sundsvall.messaging.service.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.event.IncomingEmailEvent;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.service.event.IncomingSmsEvent;
import se.sundsvall.messaging.service.event.IncomingWebMessageEvent;

@ExtendWith(MockitoExtension.class)
class StartupProcessorTests {

    @Mock
    private ApplicationEventPublisher mockEventPublisher;
    @Mock
    private MessageRepository mockMessageRepository;

    private StartupProcessor startupProcessor;

    @BeforeEach
    void setUp() {
        startupProcessor = new StartupProcessor(mockEventPublisher, mockMessageRepository);
    }

    @Test
    void testRun_whenNoPendingMessagesExist() {
        when(mockMessageRepository.findLatestWithStatus(eq(MessageStatus.PENDING))).thenReturn(List.of());

        startupProcessor.run();

        verify(mockMessageRepository, times(1)).findLatestWithStatus(eq(MessageStatus.PENDING));
        verify(mockEventPublisher, never()).publishEvent(any());
    }

    @Test
    void testRun() {
        var messages = List.of(
            MessageEntity.builder()
                .withMessageId("messageId1")
                .withType(MessageType.MESSAGE)
                .build(),
            MessageEntity.builder()
                .withMessageId("messageId2")
                .withType(MessageType.EMAIL)
                .build(),
            MessageEntity.builder()
                .withMessageId("messageId3")
                .withType(MessageType.SMS)
                .build(),
            MessageEntity.builder()
                .withMessageId("messageId4")
                .withType(MessageType.WEB_MESSAGE)
                .build());

        when(mockMessageRepository.findLatestWithStatus(eq(MessageStatus.PENDING))).thenReturn(messages);

        startupProcessor.run();

        verify(mockMessageRepository, times(1)).findLatestWithStatus(eq(MessageStatus.PENDING));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingMessageEvent.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingEmailEvent.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingSmsEvent.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingWebMessageEvent.class));
    }
}
