package se.sundsvall.messaging.integration.digitalmailsender;

import org.springframework.boot.context.properties.ConfigurationProperties;

import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.digital-mail-sender")
class DigitalMailSenderIntegrationProperties extends AbstractRestIntegrationProperties {

}
