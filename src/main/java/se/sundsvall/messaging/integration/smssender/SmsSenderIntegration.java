package se.sundsvall.messaging.integration.smssender;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.messaging.dto.SmsDto;
import se.sundsvall.messaging.integration.AbstractRestIntegration;

@Component
public class SmsSenderIntegration extends AbstractRestIntegration {

    private final RestTemplate restTemplate;
    private final SmsSenderIntegrationMapper mapper;

    public SmsSenderIntegration(final SmsSenderIntegrationMapper mapper,
            @Qualifier("integration.sms-sender.resttemplate") final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }

    public ResponseEntity<Boolean> sendSms(final SmsDto smsDto) {
        var request = mapper.toRequest(smsDto);

        try {
            return restTemplate.postForEntity("/send/sms", createRequestEntity(request), Boolean.class);
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
