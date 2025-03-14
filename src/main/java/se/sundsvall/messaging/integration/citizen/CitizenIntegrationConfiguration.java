package se.sundsvall.messaging.integration.citizen;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.messaging.integration.citizen.CitizenIntegration.INTEGRATION_NAME;

import feign.Request;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Import(FeignConfiguration.class)
class CitizenIntegrationConfiguration {

	private final CitizenIntegrationProperties properties;

	CitizenIntegrationConfiguration(final CitizenIntegrationProperties properties) {
		this.properties = properties;
	}

	@Bean
	FeignBuilderCustomizer feignCustomizer() {
		return FeignMultiCustomizer.create()
			.withRetryableOAuth2InterceptorForClientRegistration(clientRegistration())
			.withErrorDecoder(new ProblemErrorDecoder(INTEGRATION_NAME, List.of(NOT_FOUND.value())))
			.withRequestOptions(requestOptions())
			.composeCustomizersToOne();
	}

	private ClientRegistration clientRegistration() {
		return ClientRegistration
			.withRegistrationId(INTEGRATION_NAME)
			.tokenUri(properties.getTokenUrl())
			.clientId(properties.getClientId())
			.clientSecret(properties.getClientSecret())
			.authorizationGrantType(new AuthorizationGrantType(properties.getGrantType()))
			.build();
	}

	private Request.Options requestOptions() {
		return new Request.Options(
			properties.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS,
			properties.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS,
			true);
	}
}
