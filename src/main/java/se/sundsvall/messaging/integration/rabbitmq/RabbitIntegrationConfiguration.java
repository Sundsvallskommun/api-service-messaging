package se.sundsvall.messaging.integration.rabbitmq;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "rabbitmq.producer.enabled", havingValue = "true")
@EnableConfigurationProperties(RabbitIntegrationProperties.class)
class RabbitIntegrationConfiguration {

	/**
	 * Serializes published payloads as {@code application/json}. Spring Boot's auto-configured
	 * {@code RabbitTemplate} picks up this single {@link MessageConverter} bean automatically.
	 */
	@Bean
	MessageConverter jacksonMessageConverter() {
		return new JacksonJsonMessageConverter();
	}
}
