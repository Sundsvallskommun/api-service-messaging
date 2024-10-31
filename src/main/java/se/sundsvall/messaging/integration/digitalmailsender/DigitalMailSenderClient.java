package se.sundsvall.messaging.integration.digitalmailsender;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import generated.se.sundsvall.digitalmailsender.DigitalInvoiceRequest;
import generated.se.sundsvall.digitalmailsender.DigitalInvoiceResponse;
import generated.se.sundsvall.digitalmailsender.DigitalMailRequest;
import generated.se.sundsvall.digitalmailsender.DigitalMailResponse;

@FeignClient(
	name = DigitalMailSenderIntegration.INTEGRATION_NAME,
	url = "${integration.digital-mail-sender.base-url}",
	configuration = DigitalMailSenderIntegrationConfiguration.class)
interface DigitalMailSenderClient {

	@PostMapping("/{municipalityId}/send-digital-mail")
	ResponseEntity<DigitalMailResponse> sendDigitalMail(@PathVariable final String municipalityId, final DigitalMailRequest request);

	@PostMapping("/{municipalityId}/send-digital-invoice")
	ResponseEntity<DigitalInvoiceResponse> sendDigitalInvoice(@PathVariable final String municipalityId, final DigitalInvoiceRequest request);

}
