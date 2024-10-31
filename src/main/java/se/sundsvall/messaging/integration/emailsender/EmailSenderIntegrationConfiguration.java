package se.sundsvall.messaging.integration.emailsender;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

import feign.Request;
import feign.codec.ErrorDecoder;

@Import(FeignConfiguration.class)
class EmailSenderIntegrationConfiguration {

	private final EmailSenderIntegrationProperties properties;

	EmailSenderIntegrationConfiguration(final EmailSenderIntegrationProperties properties) {
		this.properties = properties;
	}

	@Bean
	FeignBuilderCustomizer feignCustomizer() {
		return FeignMultiCustomizer.create()
			.withRetryableOAuth2InterceptorForClientRegistration(clientRegistration())
			.withErrorDecoder(errorDecoder())
			.withRequestOptions(requestOptions())
			.composeCustomizersToOne();
	}

	private ClientRegistration clientRegistration() {
		return ClientRegistration
			.withRegistrationId(EmailSenderIntegration.INTEGRATION_NAME)
			.tokenUri(properties.getTokenUrl())
			.clientId(properties.getClientId())
			.clientSecret(properties.getClientSecret())
			.authorizationGrantType(new AuthorizationGrantType(properties.getGrantType()))
			.build();
	}

	private Request.Options requestOptions() {
		return new Request.Options(
			properties.getConnectTimeout().toMillis(), MILLISECONDS,
			properties.getReadTimeout().toMillis(), MILLISECONDS,
			true);
	}

	private ErrorDecoder errorDecoder() {
		return new ProblemErrorDecoder(EmailSenderIntegration.INTEGRATION_NAME);
	}
}
