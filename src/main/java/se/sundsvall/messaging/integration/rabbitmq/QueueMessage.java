package se.sundsvall.messaging.integration.rabbitmq;

/**
 * What every queued send request has in common, and the only thing the shared retry and outcome machinery needs to know
 * about a payload.
 * <p>
 * {@code recipientId} is postportal's correlation key for one recipient's delivery. It is what the outcome published
 * back on {@code api-fabriken.messaging.status} carries, and what every publish on the way there uses as its
 * correlation id, so a broker confirmation can be tied to the request it belongs to.
 */
public interface QueueMessage {

	String recipientId();
}
