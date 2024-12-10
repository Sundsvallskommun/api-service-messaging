package se.sundsvall.messaging.integration.snailmailsender;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.snailmail-sender")
class SnailMailSenderIntegrationProperties extends AbstractRestIntegrationProperties {

}
