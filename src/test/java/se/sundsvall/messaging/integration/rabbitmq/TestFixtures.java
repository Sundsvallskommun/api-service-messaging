package se.sundsvall.messaging.integration.rabbitmq;

import java.util.List;
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
	static final String OBJECT_ID = "f8e2bd3c-1a6b-4f5e-9d0a-2c7b1e4f6a58";

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

	static EmailQueueMessage emailQueueMessage() {
		return new EmailQueueMessage(
			"2281",
			MESSAGE_ID,
			RECIPIENT_ID,
			"97edca90-7fa8-457e-8223-aa078055465c",
			"recipient@example.com",
			"This is the subject",
			"This is the message to be sent",
			null,
			"Sundsvall",
			"noreply@sundsvall.se",
			null,
			List.of(new EmailQueueMessage.Attachment("file.txt", "text/plain", OBJECT_ID)),
			"mar14han; type=adAccount",
			"PostPortalService");
	}

	static RabbitIntegrationProperties properties() {
		return properties(5);
	}

	static RabbitIntegrationProperties properties(final int publishConfirmTimeoutSeconds) {
		return new RabbitIntegrationProperties(true, publishConfirmTimeoutSeconds, flow("sms"), flow("email"));
	}

	/**
	 * The object names as they are committed in application.yml, so a test that asserts on an exchange or a routing key
	 * is asserting on what the service actually publishes to.
	 */
	private static RabbitIntegrationProperties.Flow flow(final String channel) {
		return new RabbitIntegrationProperties.Flow(
			"api-fabriken.messaging." + channel,
			"api-fabriken.messaging." + channel + ".giveup",
			retryExchange(channel),
			deadRoutingKey(channel),
			"api-fabriken.messaging.status",
			channel + ".sent",
			channel + ".failed",
			List.of("5s", "30s", "5m"));
	}

	private static String retryExchange(final String channel) {
		if ("email".equals(channel)) {
			return "api-fabriken.messaging.email.retry";
		}
		return "api-fabriken.messaging.retry";
	}

	private static String deadRoutingKey(final String channel) {
		if ("email".equals(channel)) {
			return "email.dead";
		}
		return "dead";
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
