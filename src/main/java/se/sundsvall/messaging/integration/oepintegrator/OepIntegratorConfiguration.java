package se.sundsvall.messaging.integration.oepintegrator;

import feign.Request;
import feign.codec.ErrorDecoder;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.cloud.openfeign.support.JsonFormWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Configuration
class OepIntegratorConfiguration {

	private final OepIntegratorProperties properties;

	OepIntegratorConfiguration(final OepIntegratorProperties properties) {
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
			.withRegistrationId(OepIntegratorIntegration.INTEGRATION_NAME)
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

	private ErrorDecoder errorDecoder() {
		return new ProblemErrorDecoder(OepIntegratorIntegration.INTEGRATION_NAME, List.of(NOT_FOUND.value()));
	}

	@Bean
	JsonFormWriter jsonFormWriter() {
		// Needed for Feign to handle json objects sent as requestpart correctly
		return new JsonFormWriter();
	}
}
