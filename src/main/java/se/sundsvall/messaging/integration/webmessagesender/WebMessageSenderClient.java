package se.sundsvall.messaging.integration.webmessagesender;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import generated.se.sundsvall.webmessagesender.CreateWebMessageRequest;

@FeignClient(
    name = WebMessageSenderIntegration.INTEGRATION_NAME,
    url = "${integration.web-message-sender.base-url}",
    configuration = WebMessageSenderIntegrationConfiguration.class
)
interface WebMessageSenderClient {

    @PostMapping("/webmessages")
    ResponseEntity<Void> sendWebMessage(CreateWebMessageRequest request);
}
