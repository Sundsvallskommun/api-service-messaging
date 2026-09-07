package se.sundsvall.messaging.integration.objectstore;

import org.springframework.boot.context.properties.ConfigurationProperties;
import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

import static java.time.Duration.ofSeconds;

@ConfigurationProperties(prefix = "integration.object-store")
class ObjectStoreIntegrationProperties extends AbstractRestIntegrationProperties {

	/**
	 * Deliberately far below the inherited defaults of five and fifteen seconds. This is a local read of an object with
	 * a bounded size, and the call is made on the thread serving the send request - one shared with every other channel
	 * this service offers. Fifteen seconds of patience per attachment is how a slow object store stops messaging from
	 * answering anything at all, so the call is made to fail fast instead, where the circuit breaker can see it.
	 */
	ObjectStoreIntegrationProperties() {
		setConnectTimeout(ofSeconds(1));
		setReadTimeout(ofSeconds(3));
	}

	/**
	 * The bucket attachment objects are read from. Deliberately configuration rather than something the caller names in
	 * the request: object store has no municipality id and no authorization, so a caller-supplied bucket would turn this
	 * service into a confused deputy - anyone allowed to send an e-mail could read any object in any bucket using
	 * messaging's credentials.
	 */
	private String bucket;

	String getBucket() {
		return bucket;
	}

	void setBucket(final String bucket) {
		this.bucket = bucket;
	}
}
