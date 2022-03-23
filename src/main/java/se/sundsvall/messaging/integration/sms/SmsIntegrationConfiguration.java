package se.sundsvall.messaging.integration.sms;

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
@EnableConfigurationProperties(SmsIntegrationProperties.class)
class SmsIntegrationConfiguration {

    private final SmsIntegrationProperties smsIntegrationProperties;

    SmsIntegrationConfiguration(SmsIntegrationProperties smsIntegrationProperties) {
        this.smsIntegrationProperties = smsIntegrationProperties;
    }

    @Bean("integration.sms-sender.clientregistration")
    ClientRegistration clientRegistration() {
        return ClientRegistration.withRegistrationId("sms-sender")
            .tokenUri(smsIntegrationProperties.getTokenUrl())
            .clientId(smsIntegrationProperties.getClientId())
            .clientSecret(smsIntegrationProperties.getClientSecret())
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .build();
    }

    @Bean("integration.sms-sender.resttemplate")
    RestTemplate restTemplate(
            @Qualifier("integration.sms-sender.clientregistration") final ClientRegistration clientRegistration,
            final Logbook logbook) {
        return new RestTemplateBuilder()
            .withBaseUrl(smsIntegrationProperties.getBaseUrl())
            .withLogbook(logbook)
            .withOAuth2Client(clientRegistration)
            .build();
    }
}
