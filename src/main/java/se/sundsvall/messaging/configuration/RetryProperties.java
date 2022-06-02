package se.sundsvall.messaging.configuration;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "retry")
public class RetryProperties {

    private int maxAttempts = 3;

    private Duration initialDelay = Duration.ofSeconds(2);

    private Duration maxDelay = Duration.ofSeconds(20);
}
