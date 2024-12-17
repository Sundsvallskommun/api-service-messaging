package se.sundsvall.messaging.integration.emailsender;

import static se.sundsvall.messaging.integration.emailsender.EmailSenderIntegration.INTEGRATION_NAME;

import generated.se.sundsvall.emailsender.SendEmailRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
	name = INTEGRATION_NAME,
	url = "${integration.email-sender.base-url}",
	configuration = EmailSenderIntegrationConfiguration.class)
@CircuitBreaker(name = INTEGRATION_NAME)
interface EmailSenderClient {

	@PostMapping("/{municipalityId}/send/email")
	ResponseEntity<Void> sendEmail(@PathVariable final String municipalityId, final SendEmailRequest request);

}
