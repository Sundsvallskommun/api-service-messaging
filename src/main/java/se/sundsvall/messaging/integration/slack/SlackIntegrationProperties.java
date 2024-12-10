package se.sundsvall.messaging.integration.slack;

import static com.slack.api.methods.MethodsClient.ENDPOINT_URL_PREFIX;
import static java.util.Optional.ofNullable;
import static se.sundsvall.messaging.integration.Constants.DEFAULT_CONNECT_TIMEOUT;
import static se.sundsvall.messaging.integration.Constants.DEFAULT_READ_TIMEOUT;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "integration.slack")
record SlackIntegrationProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {

	@ConstructorBinding
	SlackIntegrationProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {
		this.baseUrl = ofNullable(baseUrl).orElse(ENDPOINT_URL_PREFIX);
		this.connectTimeout = ofNullable(connectTimeout).orElse(DEFAULT_CONNECT_TIMEOUT);
		this.readTimeout = ofNullable(readTimeout).orElse(DEFAULT_READ_TIMEOUT);
	}
}
