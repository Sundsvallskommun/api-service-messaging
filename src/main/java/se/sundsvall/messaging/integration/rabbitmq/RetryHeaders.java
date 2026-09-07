package se.sundsvall.messaging.integration.rabbitmq;

/**
 * The headers a request carries through the retry ladder. They ride on the message rather than in it, so they survive
 * the trip out to a wait queue and back without the payload - which is the contract with postportal - having to grow a
 * field for this service's own bookkeeping.
 */
public final class RetryHeaders {

	public static final String ATTEMPT = "x-attempt";
	public static final String FAILURE_REASON = "x-failure-reason";

	/**
	 * Messaging's own ids, carried so every attempt at one request shares them. History then shows one message that was
	 * tried several times, each attempt its own delivery, instead of several unrelated messages.
	 */
	public static final String MESSAGE_ID = "x-message-id";
	public static final String BATCH_ID = "x-batch-id";

	private RetryHeaders() {}
}
