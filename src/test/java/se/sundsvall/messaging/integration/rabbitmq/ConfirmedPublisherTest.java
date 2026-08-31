package se.sundsvall.messaging.integration.rabbitmq;

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verify;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.stubAck;

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
		stubAck(mockRabbitTemplate);

		assertThatNoException().isThrownBy(() -> new ConfirmedPublisher(mockRabbitTemplate, 1)
			.publish("exchange", "rk", "payload", "correlation", Map.of()));
	}

	@Test
	void publish_restoresTheInterruptFlagAndRaises() {
		// Unstubbed, so no confirm ever arrives. An interrupted thread makes the wait throw immediately.
		Thread.currentThread().interrupt();

		assertThatExceptionOfType(AmqpException.class)
			.isThrownBy(() -> new ConfirmedPublisher(mockRabbitTemplate, 1)
				.publish("exchange", "rk", "payload", "correlation", Map.of()))
			.withMessageContaining("Interrupted");

		// Swallowing the interrupt would leave the listener container unable to shut down cleanly.
		assertThat(Thread.currentThread().isInterrupted()).isTrue();
	}

	@Test
	void publish_setsEveryHeaderOnTheOutgoingMessage() {
		stubAck(mockRabbitTemplate);

		new ConfirmedPublisher(mockRabbitTemplate, 1)
			.publish("exchange", "rk", "payload", "correlation", Map.of("x-attempt", 3, "x-failure-reason", "boom"));

		final var captor = ArgumentCaptor.forClass(MessagePostProcessor.class);
		verify(mockRabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class),
			captor.capture(), any(CorrelationData.class));

		final var processed = captor.getValue().postProcessMessage(new Message("{}".getBytes(), new MessageProperties()));
		assertThat(processed.getMessageProperties().getHeaders())
			.containsEntry("x-attempt", 3)
			.containsEntry("x-failure-reason", "boom");
	}
}
