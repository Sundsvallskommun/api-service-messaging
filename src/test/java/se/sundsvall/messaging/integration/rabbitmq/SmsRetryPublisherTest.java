package se.sundsvall.messaging.integration.rabbitmq;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.properties;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.smsQueueMessage;

@ExtendWith(MockitoExtension.class)
class SmsRetryPublisherTest {

	@Mock
	private RabbitTemplate mockRabbitTemplate;

	private SmsRetryPublisher publisher(final RabbitIntegrationProperties properties) {
		ack();
		return new SmsRetryPublisher(mockRabbitTemplate, properties);
	}

	@Test
	void publishRetry_firstRepublishTakesTheShortestTier() {
		publisher(properties()).publishRetry(smsQueueMessage(), 2, "boom", messageIds());

		verify(mockRabbitTemplate).convertAndSend(eq("api-fabriken.messaging.retry"), eq("5s"), any(Object.class),
			any(MessagePostProcessor.class), any(CorrelationData.class));
	}

	@Test
	void publishRetry_thirdRepublishTakesTheLongestTier() {
		publisher(properties()).publishRetry(smsQueueMessage(), 4, "boom", messageIds());

		verify(mockRabbitTemplate).convertAndSend(eq("api-fabriken.messaging.retry"), eq("5m"), any(Object.class),
			any(MessagePostProcessor.class), any(CorrelationData.class));
	}

	@Test
	void publishGiveUp_usesTheDeadKey() {
		publisher(properties()).publishGiveUp(smsQueueMessage(), "no more tries");

		verify(mockRabbitTemplate).convertAndSend(eq("api-fabriken.messaging.retry"), eq("dead"), any(Object.class),
			any(MessagePostProcessor.class), any(CorrelationData.class));
	}

	@Test
	void publishRetry_correlatesOnRecipientId() {
		publisher(properties()).publishRetry(smsQueueMessage(), 2, "boom", messageIds());

		final var captor = ArgumentCaptor.forClass(CorrelationData.class);
		verify(mockRabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class),
			any(MessagePostProcessor.class), captor.capture());
		assertThat(captor.getValue().getId()).isEqualTo(TestFixtures.RECIPIENT_ID);
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

	private static SmsRetryPublisher.MessageIds messageIds() {
		return new SmsRetryPublisher.MessageIds(TestFixtures.MESSAGING_MESSAGE_ID, TestFixtures.BATCH_ID);
	}

	private void ack() {
		doAnswer(invocation -> {
			final var correlationData = invocation.getArgument(4, CorrelationData.class);
			((CompletableFuture<CorrelationData.Confirm>) correlationData.getFuture())
				.complete(new CorrelationData.Confirm(true, null));
			return null;
		}).when(mockRabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class),
			any(MessagePostProcessor.class), any(CorrelationData.class));
	}
}
