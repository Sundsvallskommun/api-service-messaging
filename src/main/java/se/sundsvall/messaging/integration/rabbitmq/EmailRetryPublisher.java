package se.sundsvall.messaging.integration.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * The e-mail channel's failure hub. Behaviour lives in {@link RetryPublisher}; this binds it to the e-mail block of
 * {@link RabbitIntegrationProperties}, which is what keeps the channel's wait queues and dead key its own.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
public class EmailRetryPublisher extends RetryPublisher<EmailQueueMessage> {

	EmailRetryPublisher(final RabbitTemplate rabbitTemplate, final RabbitIntegrationProperties properties) {
		super(rabbitTemplate, properties, properties.email(), "e-mail");
	}
}
