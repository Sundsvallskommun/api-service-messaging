package se.sundsvall.messaging.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RetryProperties.class)
class DefaultRetryConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultRetryConfiguration.class);

    @Bean
    CommandLineRunner logRetryConfiguration(final RetryProperties properties) {
        return args -> LOG.info("Retry configuration set to max {} attempts, {}s initial delay and {}s max delay",
            properties.getMaxAttempts(), properties.getInitialDelay().toMillis() / 1000.0, properties.getMaxDelay().toMillis() / 1000.0);
    }
}
