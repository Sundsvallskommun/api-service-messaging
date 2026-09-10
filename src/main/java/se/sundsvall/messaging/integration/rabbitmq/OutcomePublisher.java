package se.sundsvall.messaging.integration.rabbitmq;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Publishes the one terminal outcome per send request onto the outcome hub.
 * <p>
 * {@code <channel>.sent} means the sender accepted the request - accepted for delivery, not read by anyone.
 * {@code <channel>.failed} means the request has been given up on and nothing further will be attempted.
 * <p>
 * The payload record stays per channel even though the four are shaped alike. It is the wire contract postportal reads,
 * and the concrete type is what lands in the {@code __TypeId__} header, so collapsing them would change something
 * observable outside this service in exchange for saving four one-line records.
 */
public abstract class OutcomePublisher {

	static final String SENT = "SENT";
	static final String FAILED = "FAILED";

	private static final Logger LOG = LoggerFactory.getLogger(OutcomePublisher.class);

	private final ConfirmedPublisher publisher;
	private final RabbitIntegrationProperties.Flow flow;

	protected OutcomePublisher(final RabbitTemplate rabbitTemplate, final RabbitIntegrationProperties properties,
		final RabbitIntegrationProperties.Flow flow) {
		this.publisher = new ConfirmedPublisher(rabbitTemplate, properties.publishConfirmTimeoutSeconds());
		this.flow = flow;
	}

	/**
	 * Builds this channel's outcome record. Kept abstract rather than handed in as a factory so that each channel's
	 * record type is named at the point a reader is looking for it.
	 */
	protected abstract Object toStatusMessage(String recipientId, String status, String externalId, String statusDetail);

	public void publishSent(final String recipientId, final String messageId) {
		publish(flow.sentRoutingKey(), recipientId, SENT, toStatusMessage(recipientId, SENT, messageId, null));
	}

	public void publishFailed(final String recipientId, final String statusDetail) {
		publish(flow.failedRoutingKey(), recipientId, FAILED, toStatusMessage(recipientId, FAILED, null, statusDetail));
	}

	private void publish(final String routingKey, final String recipientId, final String status, final Object outcome) {
		publisher.publish(flow.statusExchange(), routingKey, outcome, recipientId, Map.of());

		LOG.info("Published outcome {} for recipient {} (exchange={}, routingKey={})",
			status, recipientId, flow.statusExchange(), routingKey);
	}
}
