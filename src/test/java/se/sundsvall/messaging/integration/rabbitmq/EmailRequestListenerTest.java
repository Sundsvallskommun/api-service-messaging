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
import se.sundsvall.messaging.api.model.request.EmailRequest;
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
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.emailQueueMessage;

@ExtendWith(MockitoExtension.class)
class EmailRequestListenerTest {

	@Mock
	private MessageService mockMessageService;

	@Mock
	private EmailOutcomePublisher mockOutcomePublisher;

	@Mock
	private EmailRetryPublisher mockRetryPublisher;

	private EmailRequestListener listener() {
		return new EmailRequestListener(mockMessageService, mockOutcomePublisher, mockRetryPublisher);
	}

	private void whenSendEmailReturns(final MessageStatus status, final String messageId) {
		when(mockMessageService.sendEmail(any(EmailRequest.class), anyString(), anyString()))
			.thenReturn(InternalDeliveryResult.builder().withMessageId(messageId).withStatus(status).build());
	}

	private void whenSendEmailThrows(final ThrowableProblem problem) {
		when(mockMessageService.sendEmail(any(EmailRequest.class), anyString(), anyString())).thenThrow(problem);
	}

	@Test
	void receive_sent() {
		whenSendEmailReturns(MessageStatus.SENT, MESSAGE_ID);

		listener().receive(emailQueueMessage(), null, null, null);

		verify(mockOutcomePublisher).publishSent(RECIPIENT_ID, MESSAGE_ID);
		verifyNoInteractions(mockRetryPublisher);
	}

	@Test
	void receive_attachmentReferencesReachTheDeliveryPathUnresolved() {
		whenSendEmailReturns(MessageStatus.SENT, MESSAGE_ID);

		listener().receive(emailQueueMessage(), null, null, null);

		// The listener must not fetch anything itself. Resolution belongs inside the delivery attempt, where a failure
		// to read the object is classified alongside a failure to reach email-sender rather than escaping the ladder.
		final var captor = ArgumentCaptor.forClass(EmailRequest.class);
		verify(mockMessageService).sendEmail(captor.capture(), anyString(), anyString());
		final var attachment = captor.getValue().attachments().getFirst();
		assertThat(attachment.objectId()).isEqualTo(OBJECT_ID);
		assertThat(attachment.content()).isNull();
	}

	@Test
	void receive_notSentGoesToTheLadder() {
		whenSendEmailReturns(MessageStatus.NOT_SENT, null);
		when(mockRetryPublisher.hasTierFor(2)).thenReturn(true);

		listener().receive(emailQueueMessage(), null, null, null);

		verify(mockRetryPublisher).publishRetry(any(EmailQueueMessage.class), eq(2), anyString(), any());
		verify(mockOutcomePublisher, never()).publishSent(anyString(), anyString());
	}

	@Test
	void receive_clientErrorSkipsTheLadderEntirely() {
		// BAD_GATEWAY, not BAD_REQUEST, is what a 400 from email-sender looks like by the time it arrives: dept44's
		// error decoder rewrites the status of everything it is not told to bypass, so only the type still says whose
		// fault it was. A missing attachment object surfaces the same way and is equally permanent.
		whenSendEmailThrows(new ClientProblem(BAD_GATEWAY, "attachment object does not exist"));

		listener().receive(emailQueueMessage(), null, null, null);

		// Asserting on the reason, not merely that publishGiveUp was called: the exhausted-ladder branch ends in the
		// same call, so a bare verify passes even when the classification never happened.
		final var captor = ArgumentCaptor.forClass(String.class);
		verify(mockRetryPublisher).publishGiveUp(any(EmailQueueMessage.class), captor.capture());
		assertThat(captor.getValue()).contains("rejected the request");
		verify(mockRetryPublisher, never()).publishRetry(any(), anyInt(), anyString(), any());
	}

	@Test
	void receive_serverProblemRunsTheLadderDespiteCarryingTheSameStatusAsAClientProblem() {
		whenSendEmailThrows(new ServerProblem(BAD_GATEWAY, "email-sender is down"));
		when(mockRetryPublisher.hasTierFor(2)).thenReturn(true);

		listener().receive(emailQueueMessage(), null, null, null);

		verify(mockRetryPublisher).publishRetry(any(EmailQueueMessage.class), eq(2), anyString(), any());
		verify(mockRetryPublisher, never()).publishGiveUp(any(), anyString());
	}

	@Test
	void receive_exhaustedLadderGivesUp() {
		whenSendEmailThrows(Problem.valueOf(BAD_GATEWAY, "still down"));
		when(mockRetryPublisher.hasTierFor(5)).thenReturn(false);

		listener().receive(emailQueueMessage(), 4, MESSAGING_MESSAGE_ID, BATCH_ID);

		final var captor = ArgumentCaptor.forClass(String.class);
		verify(mockRetryPublisher).publishGiveUp(any(EmailQueueMessage.class), captor.capture());
		assertThat(captor.getValue()).contains("attempts exhausted after 4 tries");
	}

	@Test
	void receive_retryReusesTheIdsItWasGiven() {
		whenSendEmailReturns(MessageStatus.SENT, MESSAGING_MESSAGE_ID);

		listener().receive(emailQueueMessage(), 2, MESSAGING_MESSAGE_ID, BATCH_ID);

		// Every attempt at one e-mail is one message in history, each attempt its own delivery.
		verify(mockMessageService).sendEmail(any(EmailRequest.class), eq(BATCH_ID), eq(MESSAGING_MESSAGE_ID));
	}
}
