package se.sundsvall.messaging.integration.emailsender;

import org.springframework.boot.context.properties.ConfigurationProperties;

import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.email-sender")
class EmailSenderIntegrationProperties extends AbstractRestIntegrationProperties {

}
