package se.sundsvall.messaging.integration.webmessagesender;

import generated.se.sundsvall.webmessagesender.CreateWebMessageRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
	name = WebMessageSenderIntegration.INTEGRATION_NAME,
	url = "${integration.web-message-sender.base-url}",
	configuration = WebMessageSenderIntegrationConfiguration.class)
interface WebMessageSenderClient {

	@PostMapping("/{municipalityId}/webmessages")
	ResponseEntity<Void> sendWebMessage(@PathVariable final String municipalityId, final CreateWebMessageRequest request);

}
