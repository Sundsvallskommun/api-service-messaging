package se.sundsvall.messaging.integration.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;

import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.sms-sender")
public class SmsIntegrationProperties extends AbstractRestIntegrationProperties {

    private int messageRetries;
}
