package se.sundsvall.messaging.integration.snailmailsender;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.dto.SnailmailDto;

@Component
@EnableConfigurationProperties(SnailmailSenderIntegrationProperties.class)
public class SnailmailSenderIntegration {

    static final String INTEGRATION_NAME = "SnailmailSender";

    private final SnailmailSenderClient client;
    private final SnailmailSenderIntegrationMapper mapper;

    public SnailmailSenderIntegration(final SnailmailSenderClient client,
            final SnailmailSenderIntegrationMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public ResponseEntity<Void> sendSnailmail(final SnailmailDto snailmailDto) {
        var request = mapper.toSendSnailmailRequest(snailmailDto);

        return client.sendSnailmail(request);
    }
}
