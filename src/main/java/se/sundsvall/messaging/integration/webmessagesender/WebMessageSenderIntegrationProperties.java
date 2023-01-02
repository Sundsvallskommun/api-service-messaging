package se.sundsvall.messaging.integration.webmessagesender;

import org.springframework.boot.context.properties.ConfigurationProperties;

import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.web-message-sender")
class WebMessageSenderIntegrationProperties extends AbstractRestIntegrationProperties {

}
