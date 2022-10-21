package se.sundsvall.messaging.integration.snailmailsender;

import generated.se.sundsvall.snailmail.SendSnailMailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(
        name = SnailmailSenderIntegration.INTEGRATION_NAME,
        url = "${integration.snailmail-sender.base-url}",
        configuration = SnailmailSenderIntegrationConfiguration.class
)
public interface SnailmailSenderClient {

    @PostMapping("/send/snailmail")
    ResponseEntity<Void> sendSnailmail(SendSnailMailRequest request);
}
