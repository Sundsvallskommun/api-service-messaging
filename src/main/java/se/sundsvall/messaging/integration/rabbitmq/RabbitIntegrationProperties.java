package se.sundsvall.messaging.integration.rabbitmq;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Object names are the contract with postportal and are declared by the messaging-topology-operator via GitOps - this
 * service never declares them. The backoff schedule is the one thing here that is genuinely ours: it drives which
 * routing key a failed attempt is republished on, so changing it needs an application deploy and no topology sync.
 * <p>
 * One block per channel, because the two cannot share their failure path. The retry exchange is direct, so a queue
 * bound on a matching key receives a copy of everything published on it: e-mail tiers bound on the same keys as SMS
 * would each be handed a copy of every SMS retry, and after the wait queue's TTL that copy re-enters the other
 * channel's work queue. Separate exchanges make the leak impossible rather than merely unlikely, and are why
 * {@code retryTiers} can hold the same three values in both blocks without them meaning the same queues.
 */
@ConfigurationProperties(prefix = "rabbitmq")
public record RabbitIntegrationProperties(

	@DefaultValue("false") boolean enabled,

	@DefaultValue("5") int publishConfirmTimeoutSeconds,

	@DefaultValue Flow sms,

	@DefaultValue Flow email) {

	public record Flow(

		String workQueue,

		String giveUpQueue,

		String retryExchange,

		String deadRoutingKey,

		String statusExchange,

		String sentRoutingKey,

		String failedRoutingKey,

		/**
		 * One routing key per backoff tier, in order. Attempt N that fails is republished on tier N; once the tiers run
		 * out the request is given up on. Three tiers therefore means four delivery attempts in total.
		 */
		@DefaultValue({
			"5s", "30s", "5m"
		}) List<String> retryTiers) {
	}
}
