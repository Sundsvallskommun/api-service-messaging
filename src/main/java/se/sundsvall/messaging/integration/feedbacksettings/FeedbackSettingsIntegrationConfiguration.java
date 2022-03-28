package se.sundsvall.messaging.integration.feedbacksettings;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.client.RestTemplate;
import org.zalando.logbook.Logbook;

import se.sundsvall.dept44.configuration.resttemplate.RestTemplateBuilder;

@Configuration
@EnableConfigurationProperties(FeedbackSettingsIntegrationProperties.class)
class FeedbackSettingsIntegrationConfiguration {

    private final FeedbackSettingsIntegrationProperties properties;

    FeedbackSettingsIntegrationConfiguration(final FeedbackSettingsIntegrationProperties properties) {
        this.properties = properties;
    }

    @Bean("integration.feedback-settings.clientregistration")
    ClientRegistration clientRegistration() {
        return ClientRegistration.withRegistrationId("feedback-settings")
            .tokenUri(properties.getTokenUrl())
            .clientId(properties.getClientId())
            .clientSecret(properties.getClientSecret())
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .build();
    }

    @Bean("integration.feedback-settings.resttemplate")
    RestTemplate restTemplate(
            @Qualifier("integration.feedback-settings.clientregistration") final ClientRegistration clientRegistration,
            final Logbook logbook) {
        return new RestTemplateBuilder()
            .withBaseUrl(properties.getBaseUrl())
            .withLogbook(logbook)
            .withOAuth2Client(clientRegistration)
            .build();
    }
}
