package se.sundsvall.messaging.integration.snailmailsender;

import org.springframework.boot.context.properties.ConfigurationProperties;

import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.snailmail-sender")
class SnailMailSenderIntegrationProperties extends AbstractRestIntegrationProperties {

}