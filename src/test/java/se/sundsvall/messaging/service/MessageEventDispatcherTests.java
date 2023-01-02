package se.sundsvall.messaging.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MessageEventDispatcherTests {

    @Mock
    private ApplicationEventPublisher mockEventPublisher;
    @Mock
    private DbIntegration mockDbIntegration;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private MessageMapper mockMapper;

    @InjectMocks
    private MessageEventDispatcher messageEventDispatcher;

    @Test
    void test_handleMessageRequest() {
        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(Message.builder().build());

        messageEventDispatcher.handleMessageRequest(MessageRequest.builder()
            .withMessages(List.of(MessageRequest.Message.builder().build()))
            .build());

        verify(mockMapper, times(1)).toMessage(any(String.class), any(MessageRequest.Message.class));
        verify(mockDbIntegration, times(1)).saveMessage(any(Message.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void test_handleEmailRequest() {
        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(Message.builder().build());

        messageEventDispatcher.handleEmailRequest(EmailRequest.builder().build());

        verify(mockMapper, times(1)).toMessage(any(EmailRequest.class));
        verify(mockDbIntegration, times(1)).saveMessage(any(Message.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void test_handleSmsRequest() {
        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(Message.builder().build());

        messageEventDispatcher.handleSmsRequest(SmsRequest.builder().build());

        verify(mockMapper, times(1)).toMessage(any(SmsRequest.class));
        verify(mockDbIntegration, times(1)).saveMessage(any(Message.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void test_handleWebMessageRequest() {
        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(Message.builder().build());

        messageEventDispatcher.handleWebMessageRequest(WebMessageRequest.builder().build());

        verify(mockMapper, times(1)).toMessage(any(WebMessageRequest.class));
        verify(mockDbIntegration, times(1)).saveMessage(any(Message.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void test_handleDigitalMailRequest() {
        when(mockDbIntegration.saveMessages(any())).thenReturn(List.of(Message.builder().build()));

        messageEventDispatcher.handleDigitalMailRequest(DigitalMailRequest.builder()
                .withParty(DigitalMailRequest.Party.builder()
                    .withPartyIds(List.of("somePartyId"))
                    .build())
            .build());

        verify(mockMapper, times(1)).toMessages(any(DigitalMailRequest.class), any(String.class));
        verify(mockDbIntegration, times(1)).saveMessages(any());
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void test_handleSnailMailRequest() {
        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(Message.builder().build());

        messageEventDispatcher.handleSnailMailRequest(SnailMailRequest.builder().build());

        verify(mockMapper, times(1)).toMessage(any(SnailMailRequest.class));
        verify(mockDbIntegration, times(1)).saveMessage(any(Message.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingMessageEvent.class));
    }


    @Test
    void test_handleLetterRequest() {
        when(mockDbIntegration.saveMessages(any())).thenReturn(List.of(Message.builder().build()));

        messageEventDispatcher.handleLetterRequest(LetterRequest.builder()
            .withParty(LetterRequest.Party.builder()
                .withPartyIds(List.of("somePartyId"))
                .build())
            .build());

        verify(mockMapper, times(1)).toMessages(any(LetterRequest.class), any(String.class));
        verify(mockDbIntegration, times(1)).saveMessages(any());
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingMessageEvent.class));
    }

}
