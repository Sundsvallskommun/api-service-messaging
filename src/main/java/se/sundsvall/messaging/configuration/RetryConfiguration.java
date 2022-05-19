package se.sundsvall.messaging.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableConfigurationProperties(RetryConfigurationProperties.class)
class RetryConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(RetryConfiguration.class);

    @Bean
    RetryTemplate retryTemplate(final RetryConfigurationProperties properties) {
        LOG.info("Retry configuration set to max {} attempts and {} sec. initial interval",
            properties.getMaxAttempts(), properties.getInitialInterval().getSeconds());

        var retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(properties.getMaxAttempts());

        var backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(properties.getInitialInterval().getSeconds() * 1000);
        backOffPolicy.setMultiplier(2);

        var retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }
}
