package se.sundsvall.messaging.integration.rabbitmq;

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
import static org.mockito.Mockito.verify;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.MESSAGE_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.RECIPIENT_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.properties;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.stubAck;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.stubConfirm;

@ExtendWith(MockitoExtension.class)
class SmsOutcomePublisherTest {

	private static final String STATUS_EXCHANGE = "api-fabriken.messaging.status";

	@Mock
	private RabbitTemplate mockRabbitTemplate;

	@Test
	void publishSent_carriesTheMessageIdAsExternalId() {
		stubAck(mockRabbitTemplate);

		new SmsOutcomePublisher(mockRabbitTemplate, properties()).publishSent(RECIPIENT_ID, MESSAGE_ID);

		assertThat(publishedTo("sms.sent")).isEqualTo(new SmsStatusMessage(RECIPIENT_ID, "SENT", MESSAGE_ID, null));
	}

	@Test
	void publishFailed_carriesTheReason() {
		stubAck(mockRabbitTemplate);

		new SmsOutcomePublisher(mockRabbitTemplate, properties()).publishFailed(RECIPIENT_ID, "attempts exhausted");

		assertThat(publishedTo("sms.failed")).isEqualTo(new SmsStatusMessage(RECIPIENT_ID, "FAILED", null, "attempts exhausted"));
	}

	@Test
	void publish_raisesOnNack() {
		stubConfirm(mockRabbitTemplate, new CorrelationData.Confirm(false, "queue full"), null);

		// The caller acks only after this returns, so an unconfirmed outcome must not look like a published one.
		assertThatExceptionOfType(AmqpException.class)
			.isThrownBy(() -> new SmsOutcomePublisher(mockRabbitTemplate, properties()).publishSent(RECIPIENT_ID, MESSAGE_ID))
			.withMessageContaining("Rejected by the broker");
	}

	@Test
	void publish_raisesOnUnroutable() {
		final var returned = new ReturnedMessage(new Message("{}".getBytes(), new MessageProperties()), 312, "NO_ROUTE", STATUS_EXCHANGE, "sms.sent");
		stubConfirm(mockRabbitTemplate, new CorrelationData.Confirm(true, null), returned);

		assertThatExceptionOfType(AmqpException.class)
			.isThrownBy(() -> new SmsOutcomePublisher(mockRabbitTemplate, properties()).publishSent(RECIPIENT_ID, MESSAGE_ID))
			.withMessageContaining("Not routable");
	}

	@Test
	void publish_raisesWhenNoConfirmArrives() {
		final var properties = properties();
		properties.setPublishConfirmTimeoutSeconds(1);
		// Unstubbed, so the confirm future stays uncompleted and the wait runs into the timeout.

		assertThatExceptionOfType(AmqpException.class)
			.isThrownBy(() -> new SmsOutcomePublisher(mockRabbitTemplate, properties).publishSent(RECIPIENT_ID, MESSAGE_ID))
			.withMessageContaining("No confirmation");
	}

	private SmsStatusMessage publishedTo(final String routingKey) {
		final var captor = ArgumentCaptor.forClass(SmsStatusMessage.class);
		verify(mockRabbitTemplate).convertAndSend(eq(STATUS_EXCHANGE), eq(routingKey), captor.capture(),
			any(MessagePostProcessor.class), any(CorrelationData.class));
		return captor.getValue();
	}
}
