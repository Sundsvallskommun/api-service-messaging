package se.sundsvall.messaging.integration.emailsender;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.messaging.dto.EmailDto;
import se.sundsvall.messaging.integration.AbstractRestIntegration;

@Component
public class EmailSenderIntegration extends AbstractRestIntegration {

    private final EmailSenderIntegrationMapper mapper;
    private final RestTemplate restTemplate;

    public EmailSenderIntegration(final EmailSenderIntegrationMapper mapper,
            @Qualifier("integration.email-sender.resttemplate") final RestTemplate restTemplate) {
        this.mapper = mapper;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<Void> sendEmail(final EmailDto emailDto) {
        var request = mapper.toSendEmailRequest(emailDto);

        try {
            return restTemplate.postForEntity("/send/email", createRequestEntity(request), Void.class);
        } catch (HttpStatusCodeException e) {
            throw Problem.builder()
                .withTitle("Exception when calling SmsSender")
                .withStatus(Status.BAD_GATEWAY)
                .withCause(Problem.builder()
                    .withStatus(Status.valueOf(e.getRawStatusCode()))
                    .build())
                .build();
        } catch (RestClientException e) {
            throw Problem.builder()
                .withTitle("Exception when calling SmsSender")
                .withStatus(Status.BAD_GATEWAY)
                .build();
        }
    }
}
