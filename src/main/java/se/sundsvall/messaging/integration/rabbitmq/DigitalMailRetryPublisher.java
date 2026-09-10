package se.sundsvall.messaging.integration.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * The digital mail channel's failure hub. Behaviour lives in {@link RetryPublisher}; this binds it to the digital mail
 * block of
 * {@link RabbitIntegrationProperties}, which is what keeps the channel's wait queues and dead key its own.
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
public class DigitalMailRetryPublisher extends RetryPublisher<DigitalMailQueueMessage> {

	DigitalMailRetryPublisher(final RabbitTemplate rabbitTemplate, final RabbitIntegrationProperties properties) {
		super(rabbitTemplate, properties, properties.digitalMail(), "digital mail");
	}
}
