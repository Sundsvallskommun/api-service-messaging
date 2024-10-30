package se.sundsvall.messaging.integration.snailmailsender;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import generated.se.sundsvall.snailmail.SendSnailMailRequest;

@FeignClient(
	name = SnailMailSenderIntegration.INTEGRATION_NAME,
	url = "${integration.snailmail-sender.base-url}",
	configuration = SnailMailSenderIntegrationConfiguration.class)
interface SnailMailSenderClient {

	@PostMapping("{municipalityId}/send/snailmail")
	ResponseEntity<Void> sendSnailmail(@PathVariable String municipalityId, SendSnailMailRequest request);

	@PostMapping("{municipalityId}/send/batch/{batchId}")
	ResponseEntity<Void> sendBatch(@PathVariable String municipalityId, @PathVariable String batchId);

}
