package se.sundsvall.messaging.integration.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.email-sender")
public class EmailIntegrationProperties extends AbstractRestIntegrationProperties {

    private int messageRetries;
}
