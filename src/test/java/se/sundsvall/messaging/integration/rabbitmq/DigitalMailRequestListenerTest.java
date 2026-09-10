package se.sundsvall.messaging.integration.rabbitmq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.exception.ServerProblem;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
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
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.BATCH_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.MESSAGE_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.MESSAGING_MESSAGE_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.OBJECT_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.RECIPIENT_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.digitalMailQueueMessage;

@ExtendWith(MockitoExtension.class)
class DigitalMailRequestListenerTest {

	@Mock
	private MessageService mockMessageService;

	@Mock
	private DigitalMailOutcomePublisher mockOutcomePublisher;

	@Mock
	private DigitalMailRetryPublisher mockRetryPublisher;

	private DigitalMailRequestListener listener() {
		return new DigitalMailRequestListener(mockMessageService, mockOutcomePublisher, mockRetryPublisher);
	}

	private void whenSendReturns(final MessageStatus status, final String messageId) {
		when(mockMessageService.sendDigitalMail(any(DigitalMailRequest.class), anyString(), anyString(), anyString()))
			.thenReturn(InternalDeliveryResult.builder().withMessageId(messageId).withStatus(status).build());
	}

	private void whenSendThrows(final ThrowableProblem problem) {
		when(mockMessageService.sendDigitalMail(any(DigitalMailRequest.class), anyString(), anyString(), anyString())).thenThrow(problem);
	}

	@Test
	void receive_sent() {
		whenSendReturns(MessageStatus.SENT, MESSAGE_ID);

		listener().receive(digitalMailQueueMessage(), null, null, null);

		verify(mockOutcomePublisher).publishSent(RECIPIENT_ID, MESSAGE_ID);
		verifyNoInteractions(mockRetryPublisher);
	}

	@Test
	void receive_oneRecipientPerMessage() {
		// The single-party overload, not the batch-shaped one the REST endpoint calls. A queued request is already one
		// recipient's worth of work, and asking for one delivery back is what lets a retry reuse the previous ids.
		whenSendReturns(MessageStatus.SENT, MESSAGE_ID);

		listener().receive(digitalMailQueueMessage(), null, null, null);

		final var captor = ArgumentCaptor.forClass(DigitalMailRequest.class);
		verify(mockMessageService).sendDigitalMail(captor.capture(), eq("162021005489"), anyString(), anyString());
		assertThat(captor.getValue().party().partyIds()).containsExactly("97edca90-7fa8-457e-8223-aa078055465c");
	}

	@Test
	void receive_attachmentReferencesReachTheDeliveryPathUnresolved() {
		whenSendReturns(MessageStatus.SENT, MESSAGE_ID);

		listener().receive(digitalMailQueueMessage(), null, null, null);

		// The listener must not fetch anything itself. Resolution belongs inside the delivery attempt, where a failure
		// to read the object is classified alongside a failure to reach the sender rather than escaping the ladder.
		final var captor = ArgumentCaptor.forClass(DigitalMailRequest.class);
		verify(mockMessageService).sendDigitalMail(captor.capture(), anyString(), anyString(), anyString());
		final var attachment = captor.getValue().attachments().getFirst();
		assertThat(attachment.objectId()).isEqualTo(OBJECT_ID);
		assertThat(attachment.content()).isNull();
	}

	@Test
	void receive_notSentGoesToTheLadder() {
		whenSendReturns(MessageStatus.NOT_SENT, null);
		when(mockRetryPublisher.hasTierFor(2)).thenReturn(true);

		listener().receive(digitalMailQueueMessage(), null, null, null);

		verify(mockRetryPublisher).publishRetry(any(DigitalMailQueueMessage.class), eq(2), anyString(), any());
		verify(mockOutcomePublisher, never()).publishSent(anyString(), anyString());
	}

	@Test
	void receive_clientErrorSkipsTheLadderEntirely() {
		// BAD_GATEWAY, not BAD_REQUEST, is what a 400 from the sender looks like by the time it arrives: dept44's error
		// decoder rewrites the status of everything it is not told to bypass, so only the type still says whose fault
		// it was. A missing attachment object surfaces the same way and is equally permanent.
		whenSendThrows(new ClientProblem(BAD_GATEWAY, "attachment object does not exist"));

		listener().receive(digitalMailQueueMessage(), null, null, null);

		final var captor = ArgumentCaptor.forClass(String.class);
		verify(mockRetryPublisher).publishGiveUp(any(DigitalMailQueueMessage.class), captor.capture());
		assertThat(captor.getValue()).contains("rejected the request");
		verify(mockRetryPublisher, never()).publishRetry(any(), anyInt(), anyString(), any());
	}

	@Test
	void receive_serverProblemRunsTheLadderDespiteCarryingTheSameStatusAsAClientProblem() {
		whenSendThrows(new ServerProblem(BAD_GATEWAY, "digital-mail-sender is down"));
		when(mockRetryPublisher.hasTierFor(2)).thenReturn(true);

		listener().receive(digitalMailQueueMessage(), null, null, null);

		verify(mockRetryPublisher).publishRetry(any(DigitalMailQueueMessage.class), eq(2), anyString(), any());
		verify(mockRetryPublisher, never()).publishGiveUp(any(), anyString());
	}

	@Test
	void receive_exhaustedLadderGivesUp() {
		whenSendThrows(Problem.valueOf(BAD_GATEWAY, "still down"));
		when(mockRetryPublisher.hasTierFor(5)).thenReturn(false);

		listener().receive(digitalMailQueueMessage(), 4, MESSAGING_MESSAGE_ID, BATCH_ID);

		final var captor = ArgumentCaptor.forClass(String.class);
		verify(mockRetryPublisher).publishGiveUp(any(DigitalMailQueueMessage.class), captor.capture());
		assertThat(captor.getValue()).contains("attempts exhausted after 4 tries");
	}

	@Test
	void receive_retryReusesTheIdsItWasGiven() {
		whenSendReturns(MessageStatus.SENT, MESSAGING_MESSAGE_ID);

		listener().receive(digitalMailQueueMessage(), 2, MESSAGING_MESSAGE_ID, BATCH_ID);

		// Every attempt at one letter is one message in history, each attempt its own delivery.
		verify(mockMessageService).sendDigitalMail(any(DigitalMailRequest.class), anyString(), eq(BATCH_ID), eq(MESSAGING_MESSAGE_ID));
	}
}
