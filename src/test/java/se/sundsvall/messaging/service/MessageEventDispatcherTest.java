package se.sundsvall.messaging.service;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.MUNICIPALITY_ID;
import static se.sundsvall.messaging.service.MessageServiceTest.ORGANIZATION_NUMBER;

@ExtendWith(MockitoExtension.class)
class MessageEventDispatcherTest {

	@Mock
	private ApplicationEventPublisher mockEventPublisher;

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
		final var issuer = "issuer";
		when(mockMessageMapper.toMessage(anyString(), anyString(), anyString(), anyString(), any(MessageRequest.Message.class)))
			.thenReturn(Message.builder().build());
		when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(Message.builder().build());

		messageEventDispatcher.handleMessageRequest(MessageRequest.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withOrigin(origin)
			.withIssuer(issuer)
			.withMessages(List.of(MessageRequest.Message.builder().build()))
			.build());

		verify(mockMessageMapper).toMessage(anyString(), anyString(), anyString(), anyString(), any(MessageRequest.Message.class));
		verify(mockDbIntegration).saveMessage(any(Message.class));
		verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
	}

	@Test
	void handleEmailRequest() {
		when(mockMessageMapper.toMessage(any(EmailRequest.class), anyString())).thenReturn(Message.builder().build());
		when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(Message.builder().build());

		messageEventDispatcher.handleEmailRequest(EmailRequest.builder().build());

		verify(mockMessageMapper).toMessage(any(EmailRequest.class), anyString());
		verify(mockDbIntegration).saveMessage(any(Message.class));
		verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
	}

	@Test
	void handleSmsRequest() {
		when(mockMessageMapper.toMessage(any(SmsRequest.class), anyString())).thenReturn(Message.builder().build());
		when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(Message.builder().build());

		messageEventDispatcher.handleSmsRequest(SmsRequest.builder().build());

		verify(mockMessageMapper).toMessage(any(SmsRequest.class), anyString());
		verify(mockDbIntegration).saveMessage(any(Message.class));
		verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
	}

	@Test
	void handleSmsBatchRequest() {
		final var message = Message.builder().withMunicipalityId(MUNICIPALITY_ID).build();
		final var smsRequest = SmsRequest.builder().build();
		final var party = SmsBatchRequest.Party.builder().build();
		final var smsBatchRequest = SmsBatchRequest.builder().withParties(List.of(party)).build();

		when(mockRequestMapper.toSmsRequest(any(SmsBatchRequest.class), any(SmsBatchRequest.Party.class))).thenReturn(smsRequest);
		when(mockMessageMapper.toMessage(any(SmsRequest.class), anyString())).thenReturn(message);
		when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(message);

		assertThat(messageEventDispatcher.handleSmsBatchRequest(smsBatchRequest).batchId()).isNotEmpty();

		verify(mockRequestMapper).toSmsRequest(smsBatchRequest, party);
		verify(mockMessageMapper).toMessage(eq(smsRequest), anyString());
		verify(mockDbIntegration).saveMessage(message);
		verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));

		verifyNoMoreInteractions(mockRequestMapper, mockMessageMapper, mockDbIntegration, mockEventPublisher);
	}

	@Test
	void handleWebMessageRequest() {
		when(mockMessageMapper.toMessage(any(WebMessageRequest.class), anyString())).thenReturn(Message.builder().build());
		when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(Message.builder().build());

		messageEventDispatcher.handleWebMessageRequest(WebMessageRequest.builder().build());

		verify(mockMessageMapper).toMessage(any(WebMessageRequest.class), anyString());
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
			.build(), ORGANIZATION_NUMBER);

		verify(mockMessageMapper).toMessages(any(DigitalMailRequest.class), anyString(), anyString());
		verify(mockDbIntegration).saveMessages(anyList());
		verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
	}

	@Test
	void handleDigitalInvoiceRequest() {
		when(mockMessageMapper.toMessage(any(DigitalInvoiceRequest.class), anyString())).thenReturn(Message.builder().build());
		when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(Message.builder().build());

		messageEventDispatcher.handleDigitalInvoiceRequest(DigitalInvoiceRequest.builder().build());

		verify(mockMessageMapper).toMessage(any(DigitalInvoiceRequest.class), anyString());
		verify(mockDbIntegration).saveMessage(any(Message.class));
		verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
	}

	@Test
	void handleLetterRequest() {
		when(mockMessageMapper.toMessages(any(LetterRequest.class), anyString(), anyString()))
			.thenReturn(List.of(Message.builder().build()));
		when(mockDbIntegration.saveMessages(anyList())).thenReturn(List.of(Message.builder().build()));

		messageEventDispatcher.handleLetterRequest(LetterRequest.builder()
			.withParty(LetterRequest.Party.builder()
				.withPartyIds(List.of("somePartyId"))
				.build())
			.build(), ORGANIZATION_NUMBER);

		verify(mockMessageMapper).toMessages(any(LetterRequest.class), anyString(), eq(ORGANIZATION_NUMBER));
		verify(mockDbIntegration).saveMessages(anyList());
		verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
	}

	@Test
	void handleSlackRequest() {

		when(mockMessageMapper.toMessage(any(SlackRequest.class), anyString())).thenReturn(Message.builder().build());
		when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(Message.builder().build());

		messageEventDispatcher.handleSlackRequest(SlackRequest.builder().build());

		verify(mockMessageMapper).toMessage(any(SlackRequest.class), anyString());
		verify(mockDbIntegration).saveMessage(any(Message.class));
		verify(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));
	}

	@Test
	void handleEmailBatchRequest() {
		final var validEmailBatchRequest = TestDataFactory.createValidEmailBatchRequest();

		when(mockRequestMapper.toEmailRequest(any(EmailBatchRequest.class), any(EmailBatchRequest.Party.class)))
			.thenReturn(EmailRequest.builder().build());

		final var message = TestDataFactory.createMessage(MessageType.EMAIL, "someContent");

		when(mockMessageMapper.toMessage(any(EmailRequest.class), anyString())).thenReturn(message);
		when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(message);
		doNothing().when(mockEventPublisher).publishEvent(any(IncomingMessageEvent.class));

		messageEventDispatcher.handleEmailBatchRequest(validEmailBatchRequest);

		verify(mockRequestMapper, times(2)).toEmailRequest(any(EmailBatchRequest.class), any(EmailBatchRequest.Party.class));
		verify(mockMessageMapper, times(2)).toMessage(any(EmailRequest.class), anyString());
		verify(mockEventPublisher, times(2)).publishEvent(any(IncomingMessageEvent.class));

		verifyNoMoreInteractions(mockDbIntegration, mockRequestMapper, mockMessageMapper, mockEventPublisher);
	}

}
