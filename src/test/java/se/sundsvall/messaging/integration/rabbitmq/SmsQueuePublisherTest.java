package se.sundsvall.messaging.integration.rabbitmq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import se.sundsvall.messaging.api.model.request.Priority;
import se.sundsvall.messaging.integration.smssender.SmsDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class SmsQueuePublisherTest {

	private static final String EXCHANGE = "api-fabriken.messaging";
	private static final String ROUTING_KEY = "sms";

	@Mock
	private RabbitTemplate mockRabbitTemplate;

	private SmsQueuePublisher publisher;

	@BeforeEach
	void setUp() {
		final var properties = new RabbitIntegrationProperties();
		properties.setExchange(EXCHANGE);
		properties.setRoutingKey(ROUTING_KEY);
		publisher = new SmsQueuePublisher(mockRabbitTemplate, properties);
	}

	@Test
	void publish() {
		final var dto = SmsDto.builder()
			.withSender("Sundsvall")
			.withMobileNumber("+46701234567")
			.withMessage("Hello")
			.withPriority(Priority.HIGH)
			.build();

		publisher.publish("2281", dto);

		final var captor = ArgumentCaptor.forClass(SmsQueueMessage.class);
		verify(mockRabbitTemplate, times(1)).convertAndSend(eq(EXCHANGE), eq(ROUTING_KEY), captor.capture());
		verifyNoMoreInteractions(mockRabbitTemplate);

		assertThat(captor.getValue()).isEqualTo(
			new SmsQueueMessage("2281", "Sundsvall", "+46701234567", "Hello", Priority.HIGH));
	}

	@Test
	void publishSwallowsExceptions() {
		doThrow(new AmqpException("boom"))
			.when(mockRabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class));

		final var dto = SmsDto.builder().build();

		assertThatNoException().isThrownBy(() -> publisher.publish("2281", dto));

		verify(mockRabbitTemplate, times(1)).convertAndSend(eq(EXCHANGE), eq(ROUTING_KEY), any(Object.class));
	}
}
