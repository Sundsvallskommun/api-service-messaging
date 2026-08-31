package se.sundsvall.messaging.integration.rabbitmq;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring for the SMS queue path.
 * <p>
 * Inert unless {@code rabbitmq.enabled=true}; without these beans the connection factory stays lazy and no AMQP
 * connection is opened at all. Nothing here declares topology - the AMQP user has an empty {@code configure}
 * permission, so a {@code Declarable} bean or {@code queuesToDeclare} would fail with ACCESS_REFUSED rather than drift
 * from what is committed.
 */
@Configuration
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
@EnableConfigurationProperties(RabbitIntegrationProperties.class)
class RabbitIntegrationConfiguration {

	/**
	 * Serializes payloads as {@code application/json} in both directions. Spring Boot's auto-configured
	 * {@code RabbitTemplate} and listener container factory both pick up this single {@link MessageConverter} bean.
	 */
	@Bean
	MessageConverter jacksonMessageConverter() {
		return new JacksonJsonMessageConverter();
	}
}
