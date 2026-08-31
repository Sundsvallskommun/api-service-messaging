package se.sundsvall.messaging.integration.rabbitmq;

import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

final class TestFixtures {

	static final String RECIPIENT_ID = "1f2f1ca3-d963-4cce-8c40-4a820b8758ee";
	static final String MESSAGE_ID = "5cb99a54-7ecb-4ece-afcb-7e6ec520a2be";
	/** Messaging's own ids, as carried across a retry - distinct from postportal's MESSAGE_ID above. */
	static final String MESSAGING_MESSAGE_ID = "b1c0a1de-6d5a-4b0e-9f2a-1f0a5f2e7c31";
	static final String BATCH_ID = "7d4a2e9c-0b18-4a2f-9c3d-5a6e8b1f2d40";

	private TestFixtures() {}

	static SmsQueueMessage smsQueueMessage() {
		return new SmsQueueMessage(
			"2281",
			MESSAGE_ID,
			RECIPIENT_ID,
			"97edca90-7fa8-457e-8223-aa078055465c",
			"+46701740605",
			"Sundsvall",
			"Kommunstyrelsekontoret",
			"This is the message to be sent",
			"mar14han; type=adAccount",
			"PostPortalService");
	}

	static RabbitIntegrationProperties properties() {
		final var properties = new RabbitIntegrationProperties();
		properties.setEnabled(true);
		return properties;
	}

	/**
	 * Makes the template behave like a broker that accepted the publish. Without a stub the mock does nothing, which
	 * leaves the confirm future uncompleted - that is what the timeout and interrupt tests rely on.
	 */
	static void stubAck(final RabbitTemplate rabbitTemplate) {
		stubConfirm(rabbitTemplate, new CorrelationData.Confirm(true, null), null);
	}

	static void stubConfirm(final RabbitTemplate rabbitTemplate, final CorrelationData.Confirm confirm, final ReturnedMessage returned) {
		doAnswer(invocation -> {
			final var correlationData = invocation.getArgument(4, CorrelationData.class);
			correlationData.setReturned(returned);
			correlationData.getFuture().complete(confirm);
			return null;
		}).when(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class),
			any(MessagePostProcessor.class), any(CorrelationData.class));
	}
}
