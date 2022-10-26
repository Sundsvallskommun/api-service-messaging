package se.sundsvall.messaging.integration.snailmailsender;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignHelper;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Import(FeignConfiguration.class)
public class SnailmailSenderIntegrationConfiguration {

    private final SnailmailSenderIntegrationProperties properties;

    SnailmailSenderIntegrationConfiguration(final SnailmailSenderIntegrationProperties properties) {
        this.properties = properties;
    }

    @Bean
    RequestInterceptor oAuth2RequestInterceptor() {
        return FeignHelper.oAuth2RequestInterceptor(ClientRegistration
                .withRegistrationId(SnailmailSenderIntegration.INTEGRATION_NAME)
                .tokenUri(properties.getTokenUrl())
                .clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .authorizationGrantType(new AuthorizationGrantType(properties.getGrantType()))
                .build());
    }

    @Bean
    FeignBuilderCustomizer customizer() {
        return FeignHelper.customizeRequestOptions()
                .withConnectTimeout(properties.getConnectTimeout())
                .withReadTimeout(properties.getReadTimeout())
                .build();
    }

    @Bean
    ErrorDecoder errorDecoder() {
        return new ProblemErrorDecoder(SnailmailSenderIntegration.INTEGRATION_NAME);
    }
}
