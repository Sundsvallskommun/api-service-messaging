package se.sundsvall.messaging.integration.snailmailsender;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.model.MessageStatus;

import static se.sundsvall.messaging.model.MessageStatus.NOT_SENT;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

@Component
@EnableConfigurationProperties(SnailMailSenderIntegrationProperties.class)
public class SnailMailSenderIntegration {

    static final String INTEGRATION_NAME = "SnailmailSender";

    private final SnailMailSenderClient client;
    private final SnailMailSenderIntegrationMapper mapper;

    public SnailMailSenderIntegration(final SnailMailSenderClient client,
            final SnailMailSenderIntegrationMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public MessageStatus sendSnailMail(final SnailMailDto dto) {
        var response = client.sendSnailmail(mapper.toSendSnailmailRequest(dto));

        return response.getStatusCode().is2xxSuccessful() ? SENT : NOT_SENT;
    }

    public void sendBatch(final String batchId) {
        client.sendBatch(batchId);
    }
}
