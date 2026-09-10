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
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
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
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.snailMailQueueMessage;

@ExtendWith(MockitoExtension.class)
class SnailMailRequestListenerTest {

	@Mock
	private MessageService mockMessageService;

	@Mock
	private SnailMailOutcomePublisher mockOutcomePublisher;

	@Mock
	private SnailMailRetryPublisher mockRetryPublisher;

	private SnailMailRequestListener listener() {
		return new SnailMailRequestListener(mockMessageService, mockOutcomePublisher, mockRetryPublisher);
	}

	private void whenSendReturns(final MessageStatus status, final String messageId) {
		when(mockMessageService.sendSnailMail(any(SnailMailRequest.class), anyString(), anyString()))
			.thenReturn(InternalDeliveryResult.builder().withMessageId(messageId).withStatus(status).build());
	}

	private void whenSendThrows(final ThrowableProblem problem) {
		when(mockMessageService.sendSnailMail(any(SnailMailRequest.class), anyString(), anyString())).thenThrow(problem);
	}

	@Test
	void receive_sent() {
		whenSendReturns(MessageStatus.SENT, MESSAGE_ID);

		listener().receive(snailMailQueueMessage(), null, null, null);

		verify(mockOutcomePublisher).publishSent(RECIPIENT_ID, MESSAGE_ID);
		verifyNoInteractions(mockRetryPublisher);
	}

	@Test
	void receive_firstAttemptRunsUnderPostportalsBatchIdRatherThanAFreshOne() {
		// The one channel where the batch id is not this service's own bookkeeping: snailmail-sender groups a letter's
		// recipients by it and posts the group as one job. Minting one here would scatter a letter's recipients across
		// as many batches as it has recipients.
		whenSendReturns(MessageStatus.SENT, MESSAGE_ID);

		listener().receive(snailMailQueueMessage(), null, null, null);

		verify(mockMessageService).sendSnailMail(any(SnailMailRequest.class), eq(BATCH_ID), anyString());
	}

	@Test
	void receive_addressTravelsWithTheRequest() {
		whenSendReturns(MessageStatus.SENT, MESSAGE_ID);

		listener().receive(snailMailQueueMessage(), null, null, null);

		final var captor = ArgumentCaptor.forClass(SnailMailRequest.class);
		verify(mockMessageService).sendSnailMail(captor.capture(), anyString(), anyString());
		assertThat(captor.getValue().address().zipCode()).isEqualTo("12345");
		assertThat(captor.getValue().folderName()).isEqualTo("Sundsvalls Kommun");
	}

	@Test
	void receive_attachmentReferencesReachTheDeliveryPathUnresolved() {
		whenSendReturns(MessageStatus.SENT, MESSAGE_ID);

		listener().receive(snailMailQueueMessage(), null, null, null);

		// The attachments are the letter here, so this is the channel where inlining them on the queue would hurt
		// most - the reference has to survive all the way into the delivery attempt.
		final var captor = ArgumentCaptor.forClass(SnailMailRequest.class);
		verify(mockMessageService).sendSnailMail(captor.capture(), anyString(), anyString());
		final var attachment = captor.getValue().attachments().getFirst();
		assertThat(attachment.objectId()).isEqualTo(OBJECT_ID);
		assertThat(attachment.content()).isNull();
	}

	@Test
	void receive_notSentGoesToTheLadder() {
		whenSendReturns(MessageStatus.NOT_SENT, null);
		when(mockRetryPublisher.hasTierFor(2)).thenReturn(true);

		listener().receive(snailMailQueueMessage(), null, null, null);

		verify(mockRetryPublisher).publishRetry(any(SnailMailQueueMessage.class), eq(2), anyString(), any());
		verify(mockOutcomePublisher, never()).publishSent(anyString(), anyString());
	}

	@Test
	void receive_clientErrorSkipsTheLadderEntirely() {
		whenSendThrows(new ClientProblem(BAD_GATEWAY, "attachment object does not exist"));

		listener().receive(snailMailQueueMessage(), null, null, null);

		final var captor = ArgumentCaptor.forClass(String.class);
		verify(mockRetryPublisher).publishGiveUp(any(SnailMailQueueMessage.class), captor.capture());
		assertThat(captor.getValue()).contains("rejected the request");
		verify(mockRetryPublisher, never()).publishRetry(any(), anyInt(), anyString(), any());
	}

	@Test
	void receive_serverProblemRunsTheLadderDespiteCarryingTheSameStatusAsAClientProblem() {
		whenSendThrows(new ServerProblem(BAD_GATEWAY, "snailmail-sender is down"));
		when(mockRetryPublisher.hasTierFor(2)).thenReturn(true);

		listener().receive(snailMailQueueMessage(), null, null, null);

		verify(mockRetryPublisher).publishRetry(any(SnailMailQueueMessage.class), eq(2), anyString(), any());
		verify(mockRetryPublisher, never()).publishGiveUp(any(), anyString());
	}

	@Test
	void receive_exhaustedLadderGivesUp() {
		whenSendThrows(Problem.valueOf(BAD_GATEWAY, "still down"));
		when(mockRetryPublisher.hasTierFor(5)).thenReturn(false);

		listener().receive(snailMailQueueMessage(), 4, MESSAGING_MESSAGE_ID, BATCH_ID);

		final var captor = ArgumentCaptor.forClass(String.class);
		verify(mockRetryPublisher).publishGiveUp(any(SnailMailQueueMessage.class), captor.capture());
		assertThat(captor.getValue()).contains("attempts exhausted after 4 tries");
	}

	@Test
	void receive_retryReusesTheIdsItWasGiven() {
		whenSendReturns(MessageStatus.SENT, MESSAGING_MESSAGE_ID);

		listener().receive(snailMailQueueMessage(), 2, MESSAGING_MESSAGE_ID, BATCH_ID);

		verify(mockMessageService).sendSnailMail(any(SnailMailRequest.class), eq(BATCH_ID), eq(MESSAGING_MESSAGE_ID));
	}
}
