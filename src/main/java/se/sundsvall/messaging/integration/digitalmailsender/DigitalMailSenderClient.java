package se.sundsvall.messaging.integration.digitalmailsender;

import static se.sundsvall.messaging.integration.digitalmailsender.DigitalMailSenderIntegration.INTEGRATION_NAME;

import generated.se.sundsvall.digitalmailsender.DigitalInvoiceRequest;
import generated.se.sundsvall.digitalmailsender.DigitalInvoiceResponse;
import generated.se.sundsvall.digitalmailsender.DigitalMailRequest;
import generated.se.sundsvall.digitalmailsender.DigitalMailResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
	name = INTEGRATION_NAME,
	url = "${integration.digital-mail-sender.base-url}",
	configuration = DigitalMailSenderIntegrationConfiguration.class)
@CircuitBreaker(name = INTEGRATION_NAME)
interface DigitalMailSenderClient {

	@PostMapping("/{municipalityId}/{organizationNumber}/send-digital-mail")
	ResponseEntity<DigitalMailResponse> sendDigitalMail(@PathVariable final String municipalityId, @PathVariable final String organizationNumber, final DigitalMailRequest request);

	@PostMapping("/{municipalityId}/send-digital-invoice")
	ResponseEntity<DigitalInvoiceResponse> sendDigitalInvoice(@PathVariable final String municipalityId, final DigitalInvoiceRequest request);

}
