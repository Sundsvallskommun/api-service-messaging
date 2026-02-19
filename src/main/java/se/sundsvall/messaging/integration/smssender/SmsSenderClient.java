package se.sundsvall.messaging.integration.smssender;

import generated.se.sundsvall.smssender.SendSmsRequest;
import generated.se.sundsvall.smssender.SendSmsResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import static se.sundsvall.messaging.integration.smssender.SmsSenderIntegration.INTEGRATION_NAME;

@FeignClient(
	name = INTEGRATION_NAME,
	url = "${integration.sms-sender.base-url}",
	configuration = SmsSenderIntegrationConfiguration.class)
@CircuitBreaker(name = INTEGRATION_NAME)
interface SmsSenderClient {

	@PostMapping("/{municipalityId}/send/sms")
	ResponseEntity<SendSmsResponse> sendSms(@PathVariable String municipalityId, SendSmsRequest request);

}
