package se.sundsvall.messaging.configuration;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "retry")
class RetryConfigurationProperties {

    private int maxAttempts = 5;
    private Duration initialInterval = Duration.ofSeconds(1);
}
