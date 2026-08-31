package se.sundsvall.messaging.integration.rabbitmq;

import java.util.List;
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
import se.sundsvall.messaging.integration.rabbitmq.SmsRetryPublisher.MessageIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static se.sundsvall.messaging.integration.rabbitmq.SmsRetryPublisher.ATTEMPT_HEADER;
import static se.sundsvall.messaging.integration.rabbitmq.SmsRetryPublisher.BATCH_ID_HEADER;
import static se.sundsvall.messaging.integration.rabbitmq.SmsRetryPublisher.FAILURE_REASON_HEADER;
import static se.sundsvall.messaging.integration.rabbitmq.SmsRetryPublisher.MESSAGE_ID_HEADER;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.BATCH_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.MESSAGING_MESSAGE_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.RECIPIENT_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.properties;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.smsQueueMessage;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.stubAck;

@ExtendWith(MockitoExtension.class)
class SmsRetryPublisherTest {

	private static final String RETRY_EXCHANGE = "api-fabriken.messaging.retry";
	private static final MessageIds MESSAGE_IDS = new MessageIds(MESSAGING_MESSAGE_ID, BATCH_ID);

	@Mock
	private RabbitTemplate mockRabbitTemplate;

	private SmsRetryPublisher publisher() {
		stubAck(mockRabbitTemplate);
		return new SmsRetryPublisher(mockRabbitTemplate, properties());
	}

	@Test
	void publishRetry_firstRepublishTakesTheShortestTier() {
		publisher().publishRetry(smsQueueMessage(), 2, "boom", MESSAGE_IDS);

		verify(mockRabbitTemplate).convertAndSend(eq(RETRY_EXCHANGE), eq("5s"), any(Object.class),
			any(MessagePostProcessor.class), any(CorrelationData.class));
	}

	@Test
	void publishRetry_thirdRepublishTakesTheLongestTier() {
		publisher().publishRetry(smsQueueMessage(), 4, "boom", MESSAGE_IDS);

		verify(mockRabbitTemplate).convertAndSend(eq(RETRY_EXCHANGE), eq("5m"), any(Object.class),
			any(MessagePostProcessor.class), any(CorrelationData.class));
	}

	@Test
	void publishGiveUp_usesTheDeadKey() {
		publisher().publishGiveUp(smsQueueMessage(), "no more tries");

		verify(mockRabbitTemplate).convertAndSend(eq(RETRY_EXCHANGE), eq("dead"), any(Object.class),
			any(MessagePostProcessor.class), any(CorrelationData.class));
	}

	@Test
	void publishRetry_correlatesOnRecipientId() {
		publisher().publishRetry(smsQueueMessage(), 2, "boom", MESSAGE_IDS);

		final var captor = ArgumentCaptor.forClass(CorrelationData.class);
		verify(mockRabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class),
			any(MessagePostProcessor.class), captor.capture());
		assertThat(captor.getValue().getId()).isEqualTo(RECIPIENT_ID);
	}

	@Test
	void publishRetry_carriesTheAttemptAndIdsAsHeaders() {
		// The state the ladder runs on: it survives the trip through a wait queue only because it rides on the message.
		publisher().publishRetry(smsQueueMessage(), 2, "boom", MESSAGE_IDS);

		final var captor = ArgumentCaptor.forClass(MessagePostProcessor.class);
		verify(mockRabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class),
			captor.capture(), any(CorrelationData.class));

		final var processed = captor.getValue().postProcessMessage(new Message("{}".getBytes(), new MessageProperties()));
		assertThat(processed.getMessageProperties().getHeaders())
			.containsEntry(ATTEMPT_HEADER, 2)
			.containsEntry(FAILURE_REASON_HEADER, "boom")
			.containsEntry(MESSAGE_ID_HEADER, MESSAGING_MESSAGE_ID)
			.containsEntry(BATCH_ID_HEADER, BATCH_ID);
	}

	@Test
	void hasTierFor_runsOutAfterTheConfiguredTiers() {
		final var properties = properties();
		properties.setRetryTiers(List.of("5s", "30s", "5m"));
		final var publisher = new SmsRetryPublisher(mockRabbitTemplate, properties);

		// Three tiers means attempts 2, 3 and 4 are republished, and attempt 5 does not exist.
		assertThat(publisher.hasTierFor(2)).isTrue();
		assertThat(publisher.hasTierFor(4)).isTrue();
		assertThat(publisher.hasTierFor(5)).isFalse();
	}
}
