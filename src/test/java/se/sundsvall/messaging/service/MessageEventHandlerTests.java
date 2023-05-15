package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MessageEventHandlerTests {

    private final IncomingMessageEvent event = new IncomingMessageEvent(this, EMAIL, "someDeliveryId");

    @Mock
    private MessageService mockMessageService;
    @Mock
    private DbIntegration mockDbIntegration;

    @InjectMocks
    private MessageEventHandler messageEventHandler;

    @Test
    void test_handleIncomingMessageEvent() {
        when(mockDbIntegration.getMessageByDeliveryId(any(String.class)))
            .thenReturn(Optional.of(Message.builder().withType(MESSAGE).build()));

        messageEventHandler.handleIncomingMessageEvent(event);

        verify(mockDbIntegration, times(1)).getMessageByDeliveryId(any(String.class));
        verify(mockMessageService, times(1)).sendMessage(any(Message.class));
    }

    @Test
    void test_handleIncomingMessageEventWhenMessageIsNotFound() {
        when(mockDbIntegration.getMessageByDeliveryId(any(String.class))).thenReturn(Optional.empty());

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> messageEventHandler.handleIncomingMessageEvent(event))
            .withMessageStartingWith("Internal Server Error: Unable to send");

        verify(mockDbIntegration, times(1)).getMessageByDeliveryId(any(String.class));
        verify(mockMessageService, never()).sendMessage(any(Message.class));
    }
}
