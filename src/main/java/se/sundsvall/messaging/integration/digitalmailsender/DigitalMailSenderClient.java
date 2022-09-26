package se.sundsvall.messaging.integration.digitalmailsender;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import generated.se.sundsvall.digitalmailsender.DigitalMailRequest;
import generated.se.sundsvall.digitalmailsender.DigitalMailResponse;

@FeignClient(
    name = DigitalMailSenderIntegration.INTEGRATION_NAME,
    url = "${integration.digital-mail-sender.base-url}",
    configuration = DigitalMailSenderIntegrationConfiguration.class
)
public interface DigitalMailSenderClient {

    @PostMapping("/sendDigitalMail")
    ResponseEntity<DigitalMailResponse> sendDigitalMail(DigitalMailRequest request);
}