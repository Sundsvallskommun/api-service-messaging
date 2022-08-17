package se.sundsvall.messaging.integration;

import java.time.Duration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractRestIntegrationProperties {

    private String baseUrl;

    private Duration readTimeout = Duration.ofSeconds(15);
    private Duration connectTimeout = Duration.ofSeconds(5);

    private String tokenUrl;
    private String clientId;
    private String clientSecret;
    private String grantType = "client_credentials";
}
