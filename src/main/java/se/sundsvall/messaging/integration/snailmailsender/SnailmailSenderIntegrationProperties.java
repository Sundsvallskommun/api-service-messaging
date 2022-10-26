package se.sundsvall.messaging.integration.snailmailsender;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.snailmail-sender")
public class SnailmailSenderIntegrationProperties extends AbstractRestIntegrationProperties {

    private Duration pollDelay = Duration.ofSeconds(5);
    private int maxRetries = 3;
}