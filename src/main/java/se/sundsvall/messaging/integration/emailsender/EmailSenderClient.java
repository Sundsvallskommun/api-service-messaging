package se.sundsvall.messaging.integration.emailsender;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import generated.se.sundsvall.emailsender.SendEmailRequest;

@FeignClient(
	name = EmailSenderIntegration.INTEGRATION_NAME,
	url = "${integration.email-sender.base-url}",
	configuration = EmailSenderIntegrationConfiguration.class
)
interface EmailSenderClient {

	@PostMapping("/{municipalityId}/send/email")
	ResponseEntity<Void> sendEmail(@PathVariable final String municipalityId, final SendEmailRequest request);

}
