package se.sundsvall.messaging.integration.snailmailsender;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import generated.se.sundsvall.snailmail.SendSnailMailRequest;

@FeignClient(
        name = SnailMailSenderIntegration.INTEGRATION_NAME,
        url = "${integration.snailmail-sender.base-url}",
        configuration = SnailMailSenderIntegrationConfiguration.class
)
interface SnailMailSenderClient {

    @PostMapping("/send/snailmail")
    ResponseEntity<Void> sendSnailmail(SendSnailMailRequest request);
}
