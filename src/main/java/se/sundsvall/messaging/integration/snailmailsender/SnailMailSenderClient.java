package se.sundsvall.messaging.integration.snailmailsender;

import static se.sundsvall.messaging.Constants.X_ISSUER_HEADER_KEY;
import static se.sundsvall.messaging.Constants.X_ORIGIN_HEADER_KEY;
import static se.sundsvall.messaging.integration.snailmailsender.SnailMailSenderIntegration.INTEGRATION_NAME;

import generated.se.sundsvall.snailmail.SendSnailMailRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
	name = INTEGRATION_NAME,
	url = "${integration.snailmail-sender.base-url}",
	configuration = SnailMailSenderIntegrationConfiguration.class)
@CircuitBreaker(name = INTEGRATION_NAME)
interface SnailMailSenderClient {

	@PostMapping("{municipalityId}/send/snailmail")
	ResponseEntity<Void> sendSnailmail(@RequestHeader(X_ISSUER_HEADER_KEY) String xIssuer, @RequestHeader(X_ORIGIN_HEADER_KEY) String xOrigin, @PathVariable String municipalityId, SendSnailMailRequest request);

	@PostMapping("{municipalityId}/send/batch/{batchId}")
	ResponseEntity<Void> sendBatch(@PathVariable String municipalityId, @PathVariable String batchId);
}
