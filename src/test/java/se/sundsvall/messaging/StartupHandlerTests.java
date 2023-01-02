package se.sundsvall.messaging;

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

import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class StartupHandlerTests {

    @Mock
    private ApplicationEventPublisher mockEventPublisher;
    @Mock
    private DbIntegration mockDbIntegration;

    private StartupHandler startupProcessor;

    @BeforeEach
    void setUp() {
        startupProcessor = new StartupHandler(mockEventPublisher, mockDbIntegration);
    }

    @Test
    void testRun_whenNoPendingMessagesExist() {
        when(mockDbIntegration.getLatestMessagesWithStatus(eq(MessageStatus.PENDING))).thenReturn(List.of());

        startupProcessor.run();

        verify(mockDbIntegration, times(1)).getLatestMessagesWithStatus(eq(MessageStatus.PENDING));
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
                .build(),
            MessageEntity.builder()
                .withMessageId("messageId4")
                .withType(MessageType.SNAIL_MAIL)
                .build(),
            MessageEntity.builder()
                .withMessageId("messageId4")
                .withType(MessageType.LETTER)
                .build(),
            MessageEntity.builder()
                .withMessageId("messageId4")
                .withType(MessageType.DIGITAL_MAIL)
                .build());

        when(mockDbIntegration.getLatestMessagesWithStatus(eq(MessageStatus.PENDING))).thenReturn(messages);

        startupProcessor.run();

        verify(mockDbIntegration, times(1)).getLatestMessagesWithStatus(eq(MessageStatus.PENDING));
        verify(mockEventPublisher, times(7)).publishEvent(any(IncomingMessageEvent.class));
    }
}
