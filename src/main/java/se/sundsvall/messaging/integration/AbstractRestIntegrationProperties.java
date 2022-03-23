package se.sundsvall.messaging.integration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractRestIntegrationProperties {

    private String baseUrl;

    private String tokenUrl;
    private String clientId;
    private String clientSecret;
}
