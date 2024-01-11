package se.sundsvall.messaging.integration.snailmailsender;

import generated.se.sundsvall.snailmail.SendSnailMailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = SnailMailSenderIntegration.INTEGRATION_NAME,
        url = "${integration.snailmail-sender.base-url}",
        configuration = SnailMailSenderIntegrationConfiguration.class
)
interface SnailMailSenderClient {

    @PostMapping("/send/snailmail")
    ResponseEntity<Void> sendSnailmail(SendSnailMailRequest request);

    @PostMapping("/send/batch/{batchId}")
    ResponseEntity<Void> sendBatch(@PathVariable String batchId);
}
