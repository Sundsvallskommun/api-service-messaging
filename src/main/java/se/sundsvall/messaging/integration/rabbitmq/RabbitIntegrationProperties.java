package se.sundsvall.messaging.integration.rabbitmq;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "rabbitmq.producer")
class RabbitIntegrationProperties {

	private String exchange;

	private String routingKey;

}
