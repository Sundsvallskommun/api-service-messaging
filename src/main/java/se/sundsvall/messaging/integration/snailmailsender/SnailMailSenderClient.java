package se.sundsvall.messaging.integration.snailmailsender;

import static se.sundsvall.messaging.integration.snailmailsender.SnailMailSenderIntegration.INTEGRATION_NAME;

import generated.se.sundsvall.snailmail.SendSnailMailRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
	name = INTEGRATION_NAME,
	url = "${integration.snailmail-sender.base-url}",
	configuration = SnailMailSenderIntegrationConfiguration.class)
@CircuitBreaker(name = INTEGRATION_NAME)
interface SnailMailSenderClient {

	@PostMapping("{municipalityId}/send/snailmail")
	ResponseEntity<Void> sendSnailmail(@PathVariable String municipalityId, SendSnailMailRequest request);

	@PostMapping("{municipalityId}/send/batch/{batchId}")
	ResponseEntity<Void> sendBatch(@PathVariable String municipalityId, @PathVariable String batchId);

}
