package se.sundsvall.messaging.integration.smssender;

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
@EnableConfigurationProperties(SmsSenderIntegrationProperties.class)
class SmsSenderIntegrationConfiguration {

    private final SmsSenderIntegrationProperties properties;

    SmsSenderIntegrationConfiguration(final SmsSenderIntegrationProperties properties) {
        this.properties = properties;
    }

    @Bean("integration.sms-sender.clientregistration")
    ClientRegistration clientRegistration() {
        return ClientRegistration.withRegistrationId("sms-sender")
            .tokenUri(properties.getTokenUrl())
            .clientId(properties.getClientId())
            .clientSecret(properties.getClientSecret())
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .build();
    }

    @Bean("integration.sms-sender.resttemplate")
    RestTemplate restTemplate(
            @Qualifier("integration.sms-sender.clientregistration") final ClientRegistration clientRegistration,
            final Logbook logbook) {
        return new RestTemplateBuilder()
            .withBaseUrl(properties.getBaseUrl())
            .withLogbook(logbook)
            .withOAuth2Client(clientRegistration)
            .build();
    }
}
