package se.sundsvall.messaging.integration.rabbitmq;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.MESSAGE_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.RECIPIENT_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.properties;

@ExtendWith(MockitoExtension.class)
class SmsOutcomePublisherTest {

	private static final String STATUS_EXCHANGE = "api-fabriken.messaging.status";

	@Mock
	private RabbitTemplate mockRabbitTemplate;

	@Test
	void publishSent_carriesTheMessageIdAsExternalId() {
		confirm(new CorrelationData.Confirm(true, null), null);

		new SmsOutcomePublisher(mockRabbitTemplate, properties()).publishSent(RECIPIENT_ID, MESSAGE_ID);

		final var captor = ArgumentCaptor.forClass(SmsStatusMessage.class);
		verify(mockRabbitTemplate).convertAndSend(eq(STATUS_EXCHANGE), eq("sms.sent"), captor.capture(),
			any(MessagePostProcessor.class), any(CorrelationData.class));

		assertThat(captor.getValue()).isEqualTo(new SmsStatusMessage(RECIPIENT_ID, "SENT", MESSAGE_ID, null));
	}

	@Test
	void publishFailed_carriesTheReason() {
		confirm(new CorrelationData.Confirm(true, null), null);

		new SmsOutcomePublisher(mockRabbitTemplate, properties()).publishFailed(RECIPIENT_ID, "attempts exhausted");

		final var captor = ArgumentCaptor.forClass(SmsStatusMessage.class);
		verify(mockRabbitTemplate).convertAndSend(eq(STATUS_EXCHANGE), eq("sms.failed"), captor.capture(),
			any(MessagePostProcessor.class), any(CorrelationData.class));

		assertThat(captor.getValue()).isEqualTo(new SmsStatusMessage(RECIPIENT_ID, "FAILED", null, "attempts exhausted"));
	}

	@Test
	void publish_raisesOnNack() {
		confirm(new CorrelationData.Confirm(false, "queue full"), null);

		// The caller acks only after this returns, so an unconfirmed outcome must not look like a published one.
		assertThatExceptionOfType(AmqpException.class)
			.isThrownBy(() -> new SmsOutcomePublisher(mockRabbitTemplate, properties()).publishSent(RECIPIENT_ID, MESSAGE_ID))
			.withMessageContaining("Rejected by the broker");
	}

	@Test
	void publish_raisesOnUnroutable() {
		final var returned = new ReturnedMessage(new Message("{}".getBytes(), new MessageProperties()), 312, "NO_ROUTE", STATUS_EXCHANGE, "sms.sent");
		confirm(new CorrelationData.Confirm(true, null), returned);

		assertThatExceptionOfType(AmqpException.class)
			.isThrownBy(() -> new SmsOutcomePublisher(mockRabbitTemplate, properties()).publishSent(RECIPIENT_ID, MESSAGE_ID))
			.withMessageContaining("Not routable");
	}

	@Test
	void publish_raisesWhenNoConfirmArrives() {
		final var properties = properties();
		properties.setPublishConfirmTimeoutSeconds(1);
		// Leave the future uncompleted so the wait runs into the timeout.
		doAnswer(invocation -> null).when(mockRabbitTemplate).convertAndSend(any(String.class), any(String.class),
			any(Object.class), any(MessagePostProcessor.class), any(CorrelationData.class));

		assertThatExceptionOfType(AmqpException.class)
			.isThrownBy(() -> new SmsOutcomePublisher(mockRabbitTemplate, properties).publishSent(RECIPIENT_ID, MESSAGE_ID))
			.withMessageContaining("No confirmation");
	}

	private void confirm(final CorrelationData.Confirm confirm, final ReturnedMessage returnedMessage) {
		doAnswer(invocation -> {
			final var correlationData = invocation.getArgument(4, CorrelationData.class);
			correlationData.setReturned(returnedMessage);
			((CompletableFuture<CorrelationData.Confirm>) correlationData.getFuture()).complete(confirm);
			return null;
		}).when(mockRabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class),
			any(MessagePostProcessor.class), any(CorrelationData.class));
	}
}
