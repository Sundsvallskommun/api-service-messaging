package se.sundsvall.messaging.integration.rabbitmq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static se.sundsvall.messaging.integration.rabbitmq.SmsGiveUpListener.DEFAULT_REASON;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.RECIPIENT_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.smsQueueMessage;

@ExtendWith(MockitoExtension.class)
class SmsGiveUpListenerTest {

	@Mock
	private SmsOutcomePublisher mockOutcomePublisher;

	@InjectMocks
	private SmsGiveUpListener listener;

	@Test
	void receive_reportsTheReasonTheApplicationRecorded() {
		listener.receive(smsQueueMessage(), "attempts exhausted after 4 tries");

		verify(mockOutcomePublisher).publishFailed(RECIPIENT_ID, "attempts exhausted after 4 tries");
		verifyNoMoreInteractions(mockOutcomePublisher);
	}

	@Test
	void receive_stillReportsWhenTheBrokerDeadLetteredItAlone() {
		// The poison backstop leaves no reason header, because no application code ran. An outcome is still owed.
		listener.receive(smsQueueMessage(), null);

		verify(mockOutcomePublisher).publishFailed(RECIPIENT_ID, DEFAULT_REASON);
	}
}
