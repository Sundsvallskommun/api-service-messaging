package se.sundsvall.messaging.integration.webmessagesender;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.messaging.dto.WebMessageDto;
import se.sundsvall.messaging.integration.AbstractRestIntegration;

@Component
public class WebMessageSenderIntegration extends AbstractRestIntegration {

    private final WebMessageSenderIntegrationMapper mapper;
    private final RestTemplate restTemplate;

    public WebMessageSenderIntegration(final WebMessageSenderIntegrationMapper mapper,
            @Qualifier("integration.web-message-sender.resttemplate") final RestTemplate restTemplate) {
        this.mapper = mapper;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<Void> sendWebMessage(final WebMessageDto webMessageDto) {
        var request = mapper.toCreateWebMessageRequest(webMessageDto);

        try {
            return restTemplate.postForEntity("/webmessages", createRequestEntity(request), Void.class);
        } catch (HttpStatusCodeException e) {
            throw Problem.builder()
                .withTitle("Exception when calling WebMessageSender")
                .withStatus(Status.BAD_GATEWAY)
                .withCause(Problem.builder()
                    .withStatus(Status.valueOf(e.getRawStatusCode()))
                    .build())
                .build();
        } catch (RestClientException e) {
            throw Problem.builder()
                .withTitle("Exception when calling WebMessageSender")
                .withStatus(Status.BAD_GATEWAY)
                .build();
        }
    }
}
