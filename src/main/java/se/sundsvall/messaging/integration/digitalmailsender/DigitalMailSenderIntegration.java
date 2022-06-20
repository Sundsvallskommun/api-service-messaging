package se.sundsvall.messaging.integration.digitalmailsender;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.messaging.dto.DigitalMailDto;
import se.sundsvall.messaging.integration.AbstractRestIntegration;

import generated.se.sundsvall.digitalmailsender.DeliveryStatus;
import generated.se.sundsvall.digitalmailsender.DigitalMailResponse;

@Component
public class DigitalMailSenderIntegration extends AbstractRestIntegration {

    private final DigitalMailSenderIntegrationMapper mapper;
    private final RestTemplate restTemplate;

    public DigitalMailSenderIntegration(final DigitalMailSenderIntegrationMapper mapper,
            @Qualifier("integration.digital-mail-sender.resttemplate") final RestTemplate restTemplate) {
        this.mapper = mapper;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<Boolean> sendDigitalMail(final DigitalMailDto digitalMailDto) {
        var request = mapper.toDigitalMailRequest(digitalMailDto);

        try {
            var response = restTemplate.postForEntity(
                "/sendDigitalMail", createRequestEntity(request), DigitalMailResponse.class);

            return ResponseEntity.status(response.getStatusCode())
                .body(Optional.ofNullable(response.getBody())
                    .map(DigitalMailResponse::getDeliveryStatus)
                    .map(DeliveryStatus::getDelivered)
                    .orElse(false));
        } catch (HttpStatusCodeException e) {
            throw Problem.builder()
                .withTitle("Exception when calling DigitalMailSender")
                .withStatus(Status.BAD_GATEWAY)
                .withCause(Problem.builder()
                    .withStatus(Status.valueOf(e.getRawStatusCode()))
                    .build())
                .build();
        } catch (RestClientException e) {
            throw Problem.builder()
                .withTitle("Exception when calling DigitalMailSender")
                .withStatus(Status.BAD_GATEWAY)
                .build();
        }
    }
}
