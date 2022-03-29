package se.sundsvall.messaging.integration.emailsender;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import se.sundsvall.messaging.dto.EmailDto;
import se.sundsvall.messaging.integration.AbstractRestIntegration;

@Component
public class EmailSenderIntegration extends AbstractRestIntegration {

    private final RestTemplate restTemplate;
    private final EmailSenderIntegrationMapper mapper;

    public EmailSenderIntegration(final EmailSenderIntegrationMapper mapper,
            @Qualifier("integration.email-sender.resttemplate") final RestTemplate restTemplate) {
        this.mapper = mapper;
        this.restTemplate = restTemplate;
    }

    public HttpStatus sendEmail(final EmailDto emailDto) {
        var request = mapper.toRequest(emailDto);

        return restTemplate.postForEntity("/send/email", createRequestEntity(request), Void.class)
            .getStatusCode();
    }
}
