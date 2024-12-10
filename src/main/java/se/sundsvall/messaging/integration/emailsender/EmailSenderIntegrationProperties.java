package se.sundsvall.messaging.integration.emailsender;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.email-sender")
class EmailSenderIntegrationProperties extends AbstractRestIntegrationProperties {

}
