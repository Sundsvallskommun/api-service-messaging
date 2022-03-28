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
    private final SmsSenderIntegrationProperties properties;

    public SmsSenderIntegration(
            @Qualifier("integration.sms-sender.resttemplate") final RestTemplate restTemplate,
            final SmsSenderIntegrationMapper mapper,
            final SmsSenderIntegrationProperties properties) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.properties = properties;
    }

    public int getMessageRetries() {
        return properties.getMessageRetries();
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
