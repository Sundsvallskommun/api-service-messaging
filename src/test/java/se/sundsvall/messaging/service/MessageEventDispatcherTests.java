package se.sundsvall.messaging.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.service.mapper.MessageMapper;
import se.sundsvall.messaging.test.annotation.UnitTest;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MessageEventDispatcherTests {

    @Mock
    private ApplicationEventPublisher mockEventPublisher;
    @Mock
    private BlacklistService mockBlacklistService;
    @Mock
    private DbIntegration mockDbIntegration;

    @Mock
    private MessageMapper mockMessageMapper;

    @InjectMocks
    private MessageEventDispatcher messageEventDispatcher;

    @Test
    void handleMessageRequest() {
        final var origin = "origin";
        when(mockMessageMapper.toMessage(any(String.class), any(MessageRequest.Message.class)))
            .thenReturn(Message.builder().build());
        when(mockDbIntegration.saveMessage(anyString(), any(Message.class))).thenReturn(Message.builder().build());

        messageEventDispatcher.handleMessageRequest(origin, MessageRequest.builder()
            .withMessages(List.of(MessageRequest.Message.builder().build()))
            .build());

        verify(mockMessageMapper).toMessage(any(String.class), any(MessageRequest.Message.class));
        verify(mockDbIntegration).saveMessage(anyString(), any(Message.class));
        verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void handleEmailRequest() {
        final var origin = "origin";
        when(mockMessageMapper.toMessage(any(EmailRequest.class))).thenReturn(Message.builder().build());
        when(mockDbIntegration.saveMessage(anyString(), any(Message.class))).thenReturn(Message.builder().build());

        messageEventDispatcher.handleEmailRequest(origin, EmailRequest.builder().build());

        verify(mockMessageMapper).toMessage(any(EmailRequest.class));
        verify(mockDbIntegration).saveMessage(anyString(), any(Message.class));
        verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void handleSmsRequest() {
        final var origin = "origin";
        when(mockMessageMapper.toMessage(any(SmsRequest.class))).thenReturn(Message.builder().build());
        when(mockDbIntegration.saveMessage(anyString(), any(Message.class))).thenReturn(Message.builder().build());

        messageEventDispatcher.handleSmsRequest(origin, SmsRequest.builder().build());

        verify(mockMessageMapper).toMessage(any(SmsRequest.class));
        verify(mockDbIntegration).saveMessage(anyString(), any(Message.class));
        verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void handleWebMessageRequest() {
        final var origin = "origin";
        when(mockMessageMapper.toMessage(any(WebMessageRequest.class))).thenReturn(Message.builder().build());
        when(mockDbIntegration.saveMessage(anyString(), any(Message.class))).thenReturn(Message.builder().build());

        messageEventDispatcher.handleWebMessageRequest(origin, WebMessageRequest.builder().build());

        verify(mockMessageMapper).toMessage(any(WebMessageRequest.class));
        verify(mockDbIntegration).saveMessage(anyString(), any(Message.class));
        verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void handleDigitalMailRequest() {
        final var origin = "origin";
        when(mockDbIntegration.saveMessages(anyString(), anyList())).thenReturn(List.of(Message.builder().build()));

        messageEventDispatcher.handleDigitalMailRequest(origin, DigitalMailRequest.builder()
                .withParty(DigitalMailRequest.Party.builder()
                    .withPartyIds(List.of("somePartyId"))
                    .build())
            .build());

        verify(mockMessageMapper).toMessages(any(DigitalMailRequest.class), any(String.class));
        verify(mockDbIntegration).saveMessages(anyString(), anyList());
        verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void handleDigitalInvoiceRequest() {
        final var origin = "origin";
        when(mockMessageMapper.toMessage(any(DigitalInvoiceRequest.class))).thenReturn(Message.builder().build());
        when(mockDbIntegration.saveMessage(anyString(), any(Message.class))).thenReturn(Message.builder().build());

        messageEventDispatcher.handleDigitalInvoiceRequest(origin, DigitalInvoiceRequest.builder().build());

        verify(mockMessageMapper).toMessage(any(DigitalInvoiceRequest.class));
        verify(mockDbIntegration).saveMessage(anyString(), any(Message.class));
        verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void handleLetterRequest() {
        final var origin = "origin";
        when(mockMessageMapper.toMessages(any(LetterRequest.class), any(String.class)))
            .thenReturn(List.of(Message.builder().build()));
        when(mockDbIntegration.saveMessages(anyString(), anyList())).thenReturn(List.of(Message.builder().build()));

        messageEventDispatcher.handleLetterRequest(origin, LetterRequest.builder()
            .withParty(LetterRequest.Party.builder()
                .withPartyIds(List.of("somePartyId"))
                .build())
            .build());

        verify(mockMessageMapper).toMessages(any(LetterRequest.class), any(String.class));
        verify(mockDbIntegration).saveMessages(anyString(), anyList());
        verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void handleSlackRequest() {
        final var origin = "origin";

        when(mockMessageMapper.toMessage(any(SlackRequest.class))).thenReturn(Message.builder().build());
        when(mockDbIntegration.saveMessage(anyString(), any(Message.class))).thenReturn(Message.builder().build());

        messageEventDispatcher.handleSlackRequest(origin, SlackRequest.builder().build());

        verify(mockMessageMapper).toMessage(any(SlackRequest.class));
        verify(mockDbIntegration).saveMessage(anyString(), any(Message.class));
        verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
    }
}
