package se.sundsvall.messaging.integration.webmessagesender;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.web-message-sender")
class WebMessageSenderIntegrationProperties extends AbstractRestIntegrationProperties {

}
