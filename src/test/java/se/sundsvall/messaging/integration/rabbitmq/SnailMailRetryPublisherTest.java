package se.sundsvall.messaging.integration.rabbitmq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.BATCH_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.MESSAGING_MESSAGE_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.RECIPIENT_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.properties;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.snailMailQueueMessage;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.stubAck;

@ExtendWith(MockitoExtension.class)
class SnailMailRetryPublisherTest {

	private static final String RETRY_EXCHANGE = "api-fabriken.messaging.snail-mail.retry";
	private static final MessageIds MESSAGE_IDS = new MessageIds(MESSAGING_MESSAGE_ID, BATCH_ID);

	@Mock
	private RabbitTemplate mockRabbitTemplate;

	private SnailMailRetryPublisher publisher() {
		stubAck(mockRabbitTemplate);
		return new SnailMailRetryPublisher(mockRabbitTemplate, properties());
	}

	@Test
	void publishRetry_firstRepublishTakesTheShortestTier() {
		publisher().publishRetry(snailMailQueueMessage(), 2, "boom", MESSAGE_IDS);

		verify(mockRabbitTemplate).convertAndSend(eq(RETRY_EXCHANGE), eq("5s"), any(Object.class),
			any(MessagePostProcessor.class), any(CorrelationData.class));
	}

	@Test
	void publishRetry_thirdRepublishTakesTheLongestTier() {
		publisher().publishRetry(snailMailQueueMessage(), 4, "boom", MESSAGE_IDS);

		verify(mockRabbitTemplate).convertAndSend(eq(RETRY_EXCHANGE), eq("5m"), any(Object.class),
			any(MessagePostProcessor.class), any(CorrelationData.class));
	}

	@Test
	void publishGiveUp_usesThisChannelsOwnDeadKey() {
		// Not the bare "dead" key SMS uses. That one feeds a consumed give-up queue, so sharing it would have the SMS
		// listener eat this channel's give-up event and report it as an SMS failure - the event is gone and postportal
		// waits forever.
		publisher().publishGiveUp(snailMailQueueMessage(), "no more tries");

		verify(mockRabbitTemplate).convertAndSend(eq(RETRY_EXCHANGE), eq("snail-mail.dead"), any(Object.class),
			any(MessagePostProcessor.class), any(CorrelationData.class));
	}

	@Test
	void publishRetry_correlatesOnRecipientId() {
		publisher().publishRetry(snailMailQueueMessage(), 2, "boom", MESSAGE_IDS);

		final var captor = ArgumentCaptor.forClass(CorrelationData.class);
		verify(mockRabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class),
			any(MessagePostProcessor.class), captor.capture());
		assertThat(captor.getValue().getId()).isEqualTo(RECIPIENT_ID);
	}

	@Test
	void publishRetry_carriesTheAttemptAndIdsAsHeaders() {
		// The state the ladder runs on: it survives the trip through a wait queue only because it rides on the message.
		publisher().publishRetry(snailMailQueueMessage(), 2, "boom", MESSAGE_IDS);

		final var captor = ArgumentCaptor.forClass(MessagePostProcessor.class);
		verify(mockRabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class),
			captor.capture(), any(CorrelationData.class));

		final var processed = captor.getValue().postProcessMessage(new Message("{}".getBytes(), new MessageProperties()));
		assertThat(processed.getMessageProperties().getHeaders())
			.containsEntry(RetryHeaders.ATTEMPT, 2)
			.containsEntry(RetryHeaders.FAILURE_REASON, "boom")
			.containsEntry(RetryHeaders.MESSAGE_ID, MESSAGING_MESSAGE_ID)
			.containsEntry(RetryHeaders.BATCH_ID, BATCH_ID);
	}

	@Test
	void hasTierFor_runsOutAfterTheConfiguredTiers() {
		final var publisher = new SnailMailRetryPublisher(mockRabbitTemplate, properties());

		// Three tiers means attempts 2, 3 and 4 are republished, and attempt 5 does not exist.
		assertThat(publisher.hasTierFor(2)).isTrue();
		assertThat(publisher.hasTierFor(4)).isTrue();
		assertThat(publisher.hasTierFor(5)).isFalse();
	}
}
