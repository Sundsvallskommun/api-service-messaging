package se.sundsvall.messaging.integration.smssender;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import generated.se.sundsvall.smssender.SendSmsRequest;
import generated.se.sundsvall.smssender.SendSmsResponse;

@FeignClient(
	name = SmsSenderIntegration.INTEGRATION_NAME,
	url = "${integration.sms-sender.base-url}",
	configuration = SmsSenderIntegrationConfiguration.class)
interface SmsSenderClient {

	@PostMapping("/{municipalityId}/send/sms")
	ResponseEntity<SendSmsResponse> sendSms(@PathVariable String municipalityId, SendSmsRequest request);

}
