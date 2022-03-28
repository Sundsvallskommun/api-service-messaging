package se.sundsvall.messaging.integration.webmessagesender;

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
@EnableConfigurationProperties(WebMessageSenderIntegrationProperties.class)
class WebMessageSenderIntegrationConfiguration {

    private final WebMessageSenderIntegrationProperties properties;

    WebMessageSenderIntegrationConfiguration(final WebMessageSenderIntegrationProperties properties) {
        this.properties = properties;
    }

    @Bean("integration.web-message-sender.clientregistration")
    ClientRegistration clientRegistration() {
        return ClientRegistration.withRegistrationId("web-message-sender")
            .tokenUri(properties.getTokenUrl())
            .clientId(properties.getClientId())
            .clientSecret(properties.getClientSecret())
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .build();
    }

    @Bean("integration.web-message-sender.resttemplate")
    RestTemplate restTemplate(
            @Qualifier("integration.web-message-sender.clientregistration") final ClientRegistration clientRegistration,
            final Logbook logbook) {
        return new RestTemplateBuilder()
                .withBaseUrl(properties.getBaseUrl())
                .withLogbook(logbook)
                .withOAuth2Client(clientRegistration)
                .build();
    }
}
