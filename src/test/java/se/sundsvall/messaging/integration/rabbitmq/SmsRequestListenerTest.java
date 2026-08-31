package se.sundsvall.messaging.integration.rabbitmq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.service.MessageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.BATCH_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.MESSAGE_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.MESSAGING_MESSAGE_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.RECIPIENT_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.smsQueueMessage;

@ExtendWith(MockitoExtension.class)
class SmsRequestListenerTest {

	@Mock
	private MessageService mockMessageService;

	@Mock
	private SmsOutcomePublisher mockOutcomePublisher;

	@Mock
	private SmsRetryPublisher mockRetryPublisher;

	private SmsRequestListener listener() {
		return new SmsRequestListener(mockMessageService, mockOutcomePublisher, mockRetryPublisher);
	}

	@Test
	void receive_sent() {
		when(mockMessageService.sendSms(any(SmsRequest.class), anyString(), anyString()))
			.thenReturn(InternalDeliveryResult.builder().withMessageId(MESSAGE_ID).withStatus(MessageStatus.SENT).build());

		listener().receive(smsQueueMessage(), null, null, null);

		verify(mockOutcomePublisher).publishSent(RECIPIENT_ID, MESSAGE_ID);
		verifyNoInteractions(mockRetryPublisher);
	}

	@Test
	void receive_passesTheContractThroughToTheOrdinaryDeliveryPath() {
		when(mockMessageService.sendSms(any(SmsRequest.class), anyString(), anyString()))
			.thenReturn(InternalDeliveryResult.builder().withMessageId(MESSAGE_ID).withStatus(MessageStatus.SENT).build());

		listener().receive(smsQueueMessage(), null, null, null);

		final var captor = ArgumentCaptor.forClass(SmsRequest.class);
		verify(mockMessageService).sendSms(captor.capture(), anyString(), anyString());
		assertThat(captor.getValue().mobileNumber()).isEqualTo("+46701740605");
		assertThat(captor.getValue().municipalityId()).isEqualTo("2281");
	}

	@Test
	void receive_notSentGoesToTheLadder() {
		when(mockMessageService.sendSms(any(SmsRequest.class), anyString(), anyString()))
			.thenReturn(InternalDeliveryResult.builder().withStatus(MessageStatus.NOT_SENT).build());
		when(mockRetryPublisher.hasTierFor(2)).thenReturn(true);

		listener().receive(smsQueueMessage(), null, null, null);

		verify(mockRetryPublisher).publishRetry(any(SmsQueueMessage.class), eq(2), anyString(), any());
		verify(mockOutcomePublisher, never()).publishSent(anyString(), anyString());
	}

	@Test
	void receive_serverErrorIsTransient() {
		when(mockMessageService.sendSms(any(SmsRequest.class), anyString(), anyString())).thenThrow(Problem.valueOf(BAD_GATEWAY, "sms-sender is down"));
		when(mockRetryPublisher.hasTierFor(3)).thenReturn(true);

		listener().receive(smsQueueMessage(), 2, MESSAGING_MESSAGE_ID, BATCH_ID);

		verify(mockRetryPublisher).publishRetry(any(SmsQueueMessage.class), eq(3), anyString(), any());
		verify(mockRetryPublisher, never()).publishGiveUp(any(), anyString());
	}

	@Test
	void receive_clientErrorSkipsTheLadderEntirely() {
		when(mockMessageService.sendSms(any(SmsRequest.class), anyString(), anyString())).thenThrow(Problem.valueOf(BAD_REQUEST, "invalid mobile number"));

		listener().receive(smsQueueMessage(), null, null, null);

		// No amount of waiting fixes a malformed request, so it goes straight to dead.
		verify(mockRetryPublisher).publishGiveUp(any(SmsQueueMessage.class), anyString());
		verify(mockRetryPublisher, never()).publishRetry(any(), anyInt(), anyString(), any());
	}

	@Test
	void receive_exhaustedLadderGivesUp() {
		when(mockMessageService.sendSms(any(SmsRequest.class), anyString(), anyString())).thenThrow(Problem.valueOf(BAD_GATEWAY, "still down"));
		when(mockRetryPublisher.hasTierFor(5)).thenReturn(false);

		listener().receive(smsQueueMessage(), 4, MESSAGING_MESSAGE_ID, BATCH_ID);

		final var captor = ArgumentCaptor.forClass(String.class);
		verify(mockRetryPublisher).publishGiveUp(any(SmsQueueMessage.class), captor.capture());
		assertThat(captor.getValue()).contains("attempts exhausted after 4 tries");
	}

	@Test
	void receive_firstAttemptMintsIdsAndPassesThemOn() {
		when(mockMessageService.sendSms(any(SmsRequest.class), anyString(), anyString()))
			.thenReturn(InternalDeliveryResult.builder().withStatus(MessageStatus.NOT_SENT).build());
		when(mockRetryPublisher.hasTierFor(2)).thenReturn(true);

		listener().receive(smsQueueMessage(), null, null, null);

		final var idsCaptor = ArgumentCaptor.forClass(SmsRetryPublisher.MessageIds.class);
		verify(mockRetryPublisher).publishRetry(any(SmsQueueMessage.class), eq(2), anyString(), idsCaptor.capture());

		// Minted here rather than inside the service, so a failed attempt still knows what to hand to the next one.
		final var batchCaptor = ArgumentCaptor.forClass(String.class);
		final var messageCaptor = ArgumentCaptor.forClass(String.class);
		verify(mockMessageService).sendSms(any(SmsRequest.class), batchCaptor.capture(), messageCaptor.capture());
		assertThat(idsCaptor.getValue().messageId()).isEqualTo(messageCaptor.getValue());
		assertThat(idsCaptor.getValue().batchId()).isEqualTo(batchCaptor.getValue());
	}

	@Test
	void receive_retryReusesTheIdsItWasGiven() {
		when(mockMessageService.sendSms(any(SmsRequest.class), anyString(), anyString()))
			.thenReturn(InternalDeliveryResult.builder().withMessageId(MESSAGING_MESSAGE_ID).withStatus(MessageStatus.SENT).build());

		listener().receive(smsQueueMessage(), 2, MESSAGING_MESSAGE_ID, BATCH_ID);

		// Every attempt at one SMS is one message in history, each attempt its own delivery.
		verify(mockMessageService).sendSms(any(SmsRequest.class), eq(BATCH_ID), eq(MESSAGING_MESSAGE_ID));
	}

	@Test
	void receive_publishFailureIsNotSwallowed() {
		// An unacked message is what lets the broker's delivery limit route it to dead, where it still becomes an
		// outcome. Swallowing this would ack a request whose outcome no longer exists anywhere.
		when(mockMessageService.sendSms(any(SmsRequest.class), anyString(), anyString()))
			.thenReturn(InternalDeliveryResult.builder().withMessageId(MESSAGE_ID).withStatus(MessageStatus.SENT).build());
		final var boom = new org.springframework.amqp.AmqpException("no confirmation");
		org.mockito.Mockito.doThrow(boom).when(mockOutcomePublisher).publishSent(anyString(), anyString());

		org.assertj.core.api.Assertions.assertThatExceptionOfType(org.springframework.amqp.AmqpException.class)
			.isThrownBy(() -> listener().receive(smsQueueMessage(), null, null, null));
	}
}
