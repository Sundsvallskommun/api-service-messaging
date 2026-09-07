package se.sundsvall.messaging.integration.objectstore;

import feign.Request;
import feign.codec.ErrorDecoder;
import java.util.List;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Import(FeignConfiguration.class)
class ObjectStoreIntegrationConfiguration {

	private final ObjectStoreIntegrationProperties properties;

	ObjectStoreIntegrationConfiguration(final ObjectStoreIntegrationProperties properties) {
		this.properties = properties;
	}

	@Bean
	FeignBuilderCustomizer objectStoreFeignCustomizer() {
		return FeignMultiCustomizer.create()
			.withRetryableOAuth2InterceptorForClientRegistration(clientRegistration())
			.withErrorDecoder(errorDecoder())
			.withRequestOptions(requestOptions())
			.composeCustomizersToOne();
	}

	private ClientRegistration clientRegistration() {
		return ClientRegistration
			.withRegistrationId(ObjectStoreIntegration.INTEGRATION_NAME)
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

	/**
	 * 404 is bypassed rather than left to the default. Without it dept44's error decoder rewrites the status of every
	 * response it is not told to bypass to BAD_GATEWAY, and an expired or unknown object - ordinary traffic against a
	 * store where objects have a time to live - would arrive here indistinguishable from the store being broken. That
	 * costs twice: the caller cannot be told the reference is bad rather than the dependency down, and 404s would count
	 * as failures towards the circuit breaker, so a handful of expired attachments would open it for everyone else.
	 */
	private ErrorDecoder errorDecoder() {
		return new ProblemErrorDecoder(ObjectStoreIntegration.INTEGRATION_NAME, List.of(NOT_FOUND.value()));
	}
}
