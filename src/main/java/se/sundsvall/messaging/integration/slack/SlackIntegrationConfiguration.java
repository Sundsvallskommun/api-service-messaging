package se.sundsvall.messaging.integration.slack;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.util.http.SlackHttpClient;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.okhttp.GzipInterceptor;
import org.zalando.logbook.okhttp.LogbookInterceptor;

@Configuration
class SlackIntegrationConfiguration {

	private final SlackIntegrationProperties properties;
	private final Logbook logbook;

	SlackIntegrationConfiguration(final SlackIntegrationProperties properties, final Logbook logbook) {
		this.properties = properties;
		this.logbook = logbook;
	}

	@Bean
	MethodsClient slackMethodsClient(final OkHttpClient okHttpClient) {
		var client = Slack.getInstance(new SlackHttpClient(okHttpClient)).methods();
		client.setEndpointUrlPrefix(properties.baseUrl());
		return client;
	}

	@Bean
	OkHttpClient okHttpClient() {
		return new OkHttpClient.Builder()
			.connectTimeout(properties.connectTimeout())
			.readTimeout(properties.readTimeout())
			.addNetworkInterceptor(new LogbookInterceptor(logbook))
			.addNetworkInterceptor(new GzipInterceptor())
			.build();
	}
}
