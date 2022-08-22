package se.sundsvall.messaging.integration.digitalmailsender;

import java.util.Optional;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.dto.DigitalMailDto;

import generated.se.sundsvall.digitalmailsender.DeliveryStatus;
import generated.se.sundsvall.digitalmailsender.DigitalMailResponse;

@Component
@EnableConfigurationProperties(DigitalMailSenderIntegrationProperties.class)
public class DigitalMailSenderIntegration {

    static final String INTEGRATION_NAME = "DigitalMailSender";

    private final DigitalMailSenderClient client;
    private final DigitalMailSenderIntegrationMapper mapper;

    public DigitalMailSenderIntegration(final DigitalMailSenderClient client,
            final DigitalMailSenderIntegrationMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public ResponseEntity<Boolean> sendDigitalMail(final DigitalMailDto digitalMailDto) {
        var request = mapper.toDigitalMailRequest(digitalMailDto);

        var response = client.sendDigitalMail(request);

        return ResponseEntity.status(response.getStatusCode())
            .body(Optional.ofNullable(response.getBody())
                .map(DigitalMailResponse::getDeliveryStatus)
                .map(DeliveryStatus::getDelivered)
                .orElse(false));
    }
}
