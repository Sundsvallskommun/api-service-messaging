package se.sundsvall.messaging.integration.rabbitmq;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static se.sundsvall.messaging.integration.rabbitmq.ConfirmedPublisher.withHeaders;

@ExtendWith(MockitoExtension.class)
class ConfirmedPublisherTest {

	@Mock
	private RabbitTemplate mockRabbitTemplate;

	@AfterEach
	void clearInterrupt() {
		// The interrupt test deliberately leaves the flag set; do not leak it into the next test on this thread.
		Thread.interrupted();
	}

	@Test
	void publish_returnsQuietlyOnAck() {
		doAnswer(invocation -> {
			final var correlationData = invocation.getArgument(4, CorrelationData.class);
			((CompletableFuture<CorrelationData.Confirm>) correlationData.getFuture())
				.complete(new CorrelationData.Confirm(true, null));
			return null;
		}).when(mockRabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class),
			any(MessagePostProcessor.class), any(CorrelationData.class));

		assertThatNoException().isThrownBy(() -> new ConfirmedPublisher(mockRabbitTemplate, 1)
			.publish("exchange", "rk", "payload", "correlation", withHeaders(Map.of())));
	}

	@Test
	void publish_restoresTheInterruptFlagAndRaises() {
		doAnswer(invocation -> null).when(mockRabbitTemplate).convertAndSend(any(String.class), any(String.class),
			any(Object.class), any(MessagePostProcessor.class), any(CorrelationData.class));

		// An interrupted thread makes the wait for a confirm throw immediately.
		Thread.currentThread().interrupt();

		assertThatExceptionOfType(AmqpException.class)
			.isThrownBy(() -> new ConfirmedPublisher(mockRabbitTemplate, 1)
				.publish("exchange", "rk", "payload", "correlation", withHeaders(Map.of())))
			.withMessageContaining("Interrupted");

		// Swallowing the interrupt would leave the listener container unable to shut down cleanly.
		assertThat(Thread.currentThread().isInterrupted()).isTrue();
	}

	@Test
	void withHeaders_setsEveryHeaderOnTheOutgoingMessage() {
		final var message = new Message("{}".getBytes(), new MessageProperties());

		final var result = withHeaders(Map.of("x-attempt", 3, "x-failure-reason", "boom")).postProcessMessage(message);

		assertThat(result.getMessageProperties().getHeaders())
			.containsEntry("x-attempt", 3)
			.containsEntry("x-failure-reason", "boom");
	}
}
