package se.sundsvall.messaging.integration.rabbitmq;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Object names are the contract with postportal and are declared by the messaging-topology-operator via GitOps - this
 * service never declares them. The backoff schedule is the one thing here that is genuinely ours: it drives which
 * routing key a failed attempt is republished on, so changing it needs an application deploy and no topology sync.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitIntegrationProperties {

	private boolean enabled;

	private String workQueue = "api-fabriken.messaging.sms";

	private String giveUpQueue = "api-fabriken.messaging.sms.giveup";

	private String retryExchange = "api-fabriken.messaging.retry";

	private String deadRoutingKey = "dead";

	private String statusExchange = "api-fabriken.messaging.status";

	private String sentRoutingKey = "sms.sent";

	private String failedRoutingKey = "sms.failed";

	/**
	 * One routing key per backoff tier, in order. Attempt N that fails is republished on tier N; once the tiers run out
	 * the request is given up on. Three tiers therefore means four delivery attempts in total.
	 */
	private List<String> retryTiers = List.of("5s", "30s", "5m");

	private int publishConfirmTimeoutSeconds = 5;
}
