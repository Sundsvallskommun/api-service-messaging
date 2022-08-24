package se.sundsvall.messaging.integration.snailmailsender;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.snailmail-sender")
public class SnailmailSenderIntegrationProperties extends AbstractRestIntegrationProperties {

    private Duration pollDelay = Duration.ofSeconds(5);
    private int maxRetries = 3;
}