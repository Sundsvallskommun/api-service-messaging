package se.sundsvall.messaging.integration.smssender;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignHelper;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;

@Import(FeignConfiguration.class)
@EnableConfigurationProperties(SmsSenderIntegrationProperties.class)
class SmsSenderIntegrationConfiguration {

    private final SmsSenderIntegrationProperties properties;

    SmsSenderIntegrationConfiguration(final SmsSenderIntegrationProperties properties) {
        this.properties = properties;
    }

    @Bean
    RequestInterceptor oAuth2RequestInterceptor() {
        return FeignHelper.oAuth2RequestInterceptor(ClientRegistration
            .withRegistrationId(SmsSenderIntegration.INTEGRATION_NAME)
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
        return new ProblemErrorDecoder(SmsSenderIntegration.INTEGRATION_NAME);
    }
}
