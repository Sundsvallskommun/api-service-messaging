package se.sundsvall.messaging.integration.rabbitmq;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Publishes and waits for the broker to confirm, so a caller can only acknowledge what the broker has taken
 * responsibility for.
 * <p>
 * Every publish on this path happens before an ack, and an unconfirmed publish must therefore raise rather than return.
 * Swallowing one would ack a request whose outcome no longer exists anywhere - postportal would wait forever, with no
 * timeout left to catch it.
 */
class ConfirmedPublisher {

	private final RabbitTemplate rabbitTemplate;
	private final int confirmTimeoutSeconds;

	ConfirmedPublisher(final RabbitTemplate rabbitTemplate, final int confirmTimeoutSeconds) {
		this.rabbitTemplate = rabbitTemplate;
		this.confirmTimeoutSeconds = confirmTimeoutSeconds;
	}

	void publish(final String exchange, final String routingKey, final Object payload, final String correlationId, final Map<String, Object> headers) {
		final var correlationData = new CorrelationData(correlationId);

		rabbitTemplate.convertAndSend(exchange, routingKey, payload, withHeaders(headers), correlationData);

		final var confirm = awaitConfirm(correlationData, exchange, routingKey);

		// A basic.return always precedes the basic.ack, so an unroutable message has been handed back by now.
		Optional.ofNullable(correlationData.getReturned()).ifPresent(returned -> {
			throw new AmqpException("Not routable by exchange %s with routing key %s: %s"
				.formatted(exchange, routingKey, returned.getReplyText()));
		});

		if (!confirm.ack()) {
			throw new AmqpException("Rejected by the broker (exchange %s, routing key %s): %s"
				.formatted(exchange, routingKey, confirm.reason()));
		}
	}

	private CorrelationData.Confirm awaitConfirm(final CorrelationData correlationData, final String exchange, final String routingKey) {
		try {
			return correlationData.getFuture().get(confirmTimeoutSeconds, SECONDS);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AmqpException("Interrupted while waiting for confirmation (exchange %s, routing key %s)".formatted(exchange, routingKey));
		} catch (final ExecutionException | TimeoutException _) {
			throw new AmqpException("No confirmation within %d seconds (exchange %s, routing key %s)"
				.formatted(confirmTimeoutSeconds, exchange, routingKey));
		}
	}

	private static MessagePostProcessor withHeaders(final Map<String, Object> headers) {
		return message -> {
			headers.forEach(message.getMessageProperties()::setHeader);
			return message;
		};
	}
}
