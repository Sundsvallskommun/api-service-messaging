package se.sundsvall.messaging.integration.digitalmailsender;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.digital-mail-sender")
class DigitalMailSenderIntegrationProperties extends AbstractRestIntegrationProperties {

}
