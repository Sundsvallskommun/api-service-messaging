package se.sundsvall.messaging.integration.feedbacksettings;

import org.springframework.boot.context.properties.ConfigurationProperties;

import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

@ConfigurationProperties(prefix = "integration.feedback-settings")
public class FeedbackSettingsIntegrationProperties extends AbstractRestIntegrationProperties {

}
