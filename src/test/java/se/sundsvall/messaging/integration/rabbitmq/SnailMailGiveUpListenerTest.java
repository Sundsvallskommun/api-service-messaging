package se.sundsvall.messaging.integration.rabbitmq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static se.sundsvall.messaging.integration.rabbitmq.SnailMailGiveUpListener.DEFAULT_REASON;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.RECIPIENT_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.snailMailQueueMessage;

@ExtendWith(MockitoExtension.class)
class SnailMailGiveUpListenerTest {

	@Mock
	private SnailMailOutcomePublisher mockOutcomePublisher;

	@InjectMocks
	private SnailMailGiveUpListener listener;

	@Test
	void receive_reportsTheReasonTheApplicationRecorded() {
		listener.receive(snailMailQueueMessage(), "attempts exhausted after 4 tries");

		verify(mockOutcomePublisher).publishFailed(RECIPIENT_ID, "attempts exhausted after 4 tries");
		verifyNoMoreInteractions(mockOutcomePublisher);
	}

	@Test
	void receive_stillReportsWhenTheBrokerDeadLetteredItAlone() {
		// The poison backstop leaves no reason header, because no application code ran. An outcome is still owed.
		listener.receive(snailMailQueueMessage(), null);

		verify(mockOutcomePublisher).publishFailed(RECIPIENT_ID, DEFAULT_REASON);
	}
}
