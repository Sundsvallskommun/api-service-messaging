package se.sundsvall.messaging.integration.emailsender;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.dto.EmailDto;
import se.sundsvall.messaging.integration.AbstractRestIntegration;

@Component
public class EmailSenderIntegration extends AbstractRestIntegration {

    static final String INTEGRATION_NAME = "EmailSender";

    private final EmailSenderClient client;
    private final EmailSenderIntegrationMapper mapper;

    public EmailSenderIntegration(final EmailSenderClient client,
            final EmailSenderIntegrationMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public ResponseEntity<Void> sendEmail(final EmailDto emailDto) {
        var request = mapper.toSendEmailRequest(emailDto);

        return client.sendEmail(request);
    }
}
