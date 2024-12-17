package se.sundsvall.messaging.integration.webmessagesender;

import static se.sundsvall.messaging.integration.webmessagesender.WebMessageSenderIntegration.INTEGRATION_NAME;

import generated.se.sundsvall.webmessagesender.CreateWebMessageRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
	name = INTEGRATION_NAME,
	url = "${integration.web-message-sender.base-url}",
	configuration = WebMessageSenderIntegrationConfiguration.class)
@CircuitBreaker(name = INTEGRATION_NAME)
interface WebMessageSenderClient {

	@PostMapping("/{municipalityId}/webmessages")
	ResponseEntity<Void> sendWebMessage(@PathVariable final String municipalityId, final CreateWebMessageRequest request);

}
