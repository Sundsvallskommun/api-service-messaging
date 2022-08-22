package se.sundsvall.messaging.integration.webmessagesender;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.dto.WebMessageDto;

@Component
@EnableConfigurationProperties(WebMessageSenderIntegrationProperties.class)
public class WebMessageSenderIntegration {

    static final String INTEGRATION_NAME = "WebMessageSender";

    private final WebMessageSenderClient client;
    private final WebMessageSenderIntegrationMapper mapper;

    public WebMessageSenderIntegration(final WebMessageSenderClient client,
            final WebMessageSenderIntegrationMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public ResponseEntity<Void> sendWebMessage(final WebMessageDto webMessageDto) {
        var request = mapper.toCreateWebMessageRequest(webMessageDto);

        return client.sendWebMessage(request);
    }
}
