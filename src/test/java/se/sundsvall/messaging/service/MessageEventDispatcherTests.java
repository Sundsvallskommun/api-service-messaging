package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.messaging.TestDataFactory;
import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailBatchRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsBatchRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.service.mapper.MessageMapper;
import se.sundsvall.messaging.service.mapper.RequestMapper;
import se.sundsvall.messaging.test.annotation.UnitTest;

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

	@Mock
	private RequestMapper mockRequestMapper;

    @InjectMocks
    private MessageEventDispatcher messageEventDispatcher;

    @Test
    void handleMessageRequest() {
        final var origin = "origin";
        when(mockMessageMapper.toMessage(anyString(), anyString(), any(MessageRequest.Message.class)))
            .thenReturn(Message.builder().build());
        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(Message.builder().build());

        messageEventDispatcher.handleMessageRequest(MessageRequest.builder()
            .withOrigin(origin)
            .withMessages(List.of(MessageRequest.Message.builder().build()))
            .build());

        verify(mockMessageMapper).toMessage(anyString(), any(String.class), any(MessageRequest.Message.class));
        verify(mockDbIntegration).saveMessage(any(Message.class));
        verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void handleEmailRequest() {
        when(mockMessageMapper.toMessage(any(EmailRequest.class))).thenReturn(Message.builder().build());
        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(Message.builder().build());

        messageEventDispatcher.handleEmailRequest(EmailRequest.builder().build());

        verify(mockMessageMapper).toMessage(any(EmailRequest.class));
        verify(mockDbIntegration).saveMessage(any(Message.class));
        verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void handleSmsRequest() {
        when(mockMessageMapper.toMessage(any(SmsRequest.class))).thenReturn(Message.builder().build());
        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(Message.builder().build());

        messageEventDispatcher.handleSmsRequest(SmsRequest.builder().build());

        verify(mockMessageMapper).toMessage(any(SmsRequest.class));
        verify(mockDbIntegration).saveMessage(any(Message.class));
        verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
    }

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void handleSmsBatchRequest(boolean whitelisted) {
		final var message = Message.builder().build();
		final var smsRequest = SmsRequest.builder().build();
		final var party = SmsBatchRequest.Party.builder().build();
		final var smsBatchRequest = SmsBatchRequest.builder().withParties(List.of(party)).build();

		if (whitelisted) {
			when(mockRequestMapper.toSmsRequest(any(SmsBatchRequest.class), any(SmsBatchRequest.Party.class))).thenReturn(smsRequest);
			when(mockMessageMapper.toMessage(any(SmsRequest.class), any(String.class))).thenReturn(message);
			when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(message);
		} else {
			doThrow(Problem.valueOf(Status.BAD_REQUEST)).when(mockBlacklistService).check(any(), any());
		}

		assertThat(messageEventDispatcher.handleSmsBatchRequest(smsBatchRequest).batchId()).isNotEmpty();

		if (whitelisted) {
			verify(mockRequestMapper).toSmsRequest(smsBatchRequest, party);
			verify(mockMessageMapper).toMessage(eq(smsRequest), any(String.class));
			verify(mockDbIntegration).saveMessage(message);
			verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
		}
		verifyNoMoreInteractions(mockRequestMapper, mockMessageMapper, mockDbIntegration, mockEventPublisher);
	}

	@Test
    void handleWebMessageRequest() {
        when(mockMessageMapper.toMessage(any(WebMessageRequest.class))).thenReturn(Message.builder().build());
        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(Message.builder().build());

        messageEventDispatcher.handleWebMessageRequest(WebMessageRequest.builder().build());

        verify(mockMessageMapper).toMessage(any(WebMessageRequest.class));
        verify(mockDbIntegration).saveMessage(any(Message.class));
        verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void handleDigitalMailRequest() {
        when(mockDbIntegration.saveMessages(anyList())).thenReturn(List.of(Message.builder().build()));

        messageEventDispatcher.handleDigitalMailRequest(DigitalMailRequest.builder()
                .withParty(DigitalMailRequest.Party.builder()
                    .withPartyIds(List.of("somePartyId"))
                    .build())
            .build());

        verify(mockMessageMapper).toMessages(any(DigitalMailRequest.class), any(String.class));
        verify(mockDbIntegration).saveMessages(anyList());
        verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void handleDigitalInvoiceRequest() {
        when(mockMessageMapper.toMessage(any(DigitalInvoiceRequest.class))).thenReturn(Message.builder().build());
        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(Message.builder().build());

        messageEventDispatcher.handleDigitalInvoiceRequest(DigitalInvoiceRequest.builder().build());

        verify(mockMessageMapper).toMessage(any(DigitalInvoiceRequest.class));
        verify(mockDbIntegration).saveMessage(any(Message.class));
        verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void handleLetterRequest() {
        when(mockMessageMapper.toMessages(any(LetterRequest.class), any(String.class)))
            .thenReturn(List.of(Message.builder().build()));
        when(mockDbIntegration.saveMessages(anyList())).thenReturn(List.of(Message.builder().build()));

        messageEventDispatcher.handleLetterRequest(LetterRequest.builder()
            .withParty(LetterRequest.Party.builder()
                .withPartyIds(List.of("somePartyId"))
                .build())
            .build());

        verify(mockMessageMapper).toMessages(any(LetterRequest.class), any(String.class));
        verify(mockDbIntegration).saveMessages(anyList());
        verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void handleSlackRequest() {

        when(mockMessageMapper.toMessage(any(SlackRequest.class))).thenReturn(Message.builder().build());
        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(Message.builder().build());

        messageEventDispatcher.handleSlackRequest(SlackRequest.builder().build());

        verify(mockMessageMapper).toMessage(any(SlackRequest.class));
        verify(mockDbIntegration).saveMessage(any(Message.class));
        verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
    }

	@Test
	void handleEmailBatchRequest() {
		var validEmailBatchRequest = TestDataFactory.createValidEmailBatchRequest();

		doNothing().when(mockBlacklistService).check(MessageType.EMAIL, "someone@somehost.com");
		when(mockRequestMapper.toEmailBatchRequest(any(EmailBatchRequest.class), any(EmailBatchRequest.Party.class)))
			.thenReturn(EmailRequest.builder().build());

		var message = TestDataFactory.createMessage(MessageType.EMAIL, "someContent");

		when(mockMessageMapper.toMessage(any(EmailRequest.class), anyString())).thenReturn(message);
		when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(message);
		doNothing().when(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));

		messageEventDispatcher.handleEmailBatchRequest(validEmailBatchRequest);

		verify(mockBlacklistService, times(2)).check(MessageType.EMAIL, "someone@somehost.com");
		verify(mockRequestMapper, times(2)).toEmailBatchRequest(any(EmailBatchRequest.class), any(EmailBatchRequest.Party.class));
		verify(mockMessageMapper, times(2)).toMessage(any(EmailRequest.class), anyString());
		verify(mockEventPublisher, times(2)).publishEvent(any(IncomingMessageEvent.class));

		verifyNoMoreInteractions(mockBlacklistService, mockDbIntegration, mockRequestMapper, mockMessageMapper, mockEventPublisher);
	}

	@Test
	void handleEmailBatch_whenEmailIsBlacklisted() {
		var validEmailBatchRequest = TestDataFactory.createValidEmailBatchRequestWithABlacklistedEmail();

		doNothing().when(mockBlacklistService).check(MessageType.EMAIL, "someone@somehost.com");
		doThrow(Problem.valueOf(Status.BAD_REQUEST)).when(mockBlacklistService).check(MessageType.EMAIL, "blacklisted@somehost.com");
		when(mockRequestMapper.toEmailBatchRequest(any(EmailBatchRequest.class), any(EmailBatchRequest.Party.class)))
			.thenReturn(EmailRequest.builder().build());

		var message = TestDataFactory.createMessage(MessageType.EMAIL, "someContent");

		when(mockMessageMapper.toMessage(any(EmailRequest.class), anyString())).thenReturn(message);
		when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(message);
		doNothing().when(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));

		messageEventDispatcher.handleEmailBatchRequest(validEmailBatchRequest);

		verify(mockBlacklistService).check(MessageType.EMAIL, "someone@somehost.com");
		verify(mockRequestMapper).toEmailBatchRequest(any(EmailBatchRequest.class), any(EmailBatchRequest.Party.class));
		verify(mockMessageMapper).toMessage(any(EmailRequest.class), anyString());
		verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));

		verifyNoMoreInteractions(mockBlacklistService, mockDbIntegration, mockRequestMapper, mockMessageMapper, mockEventPublisher);
	}
}
