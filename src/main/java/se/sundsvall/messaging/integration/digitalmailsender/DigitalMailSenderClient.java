package se.sundsvall.messaging.integration.digitalmailsender;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import generated.se.sundsvall.digitalmailsender.DigitalInvoiceRequest;
import generated.se.sundsvall.digitalmailsender.DigitalInvoiceResponse;
import generated.se.sundsvall.digitalmailsender.DigitalMailRequest;
import generated.se.sundsvall.digitalmailsender.DigitalMailResponse;

@FeignClient(
    name = DigitalMailSenderIntegration.INTEGRATION_NAME,
    url = "${integration.digital-mail-sender.base-url}",
    configuration = DigitalMailSenderIntegrationConfiguration.class
)
interface DigitalMailSenderClient {

    @PostMapping("/send-digital-mail")
    ResponseEntity<DigitalMailResponse> sendDigitalMail(DigitalMailRequest request);

    @PostMapping("/send-digital-invoice")
    ResponseEntity<DigitalInvoiceResponse> sendDigitalInvoice(DigitalInvoiceRequest request);
}
