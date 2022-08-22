package se.sundsvall.messaging.integration.smssender;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import generated.se.sundsvall.smssender.SendSmsRequest;
import generated.se.sundsvall.smssender.SendSmsResponse;

@FeignClient(
    name = SmsSenderIntegration.INTEGRATION_NAME,
    url = "${integration.sms-sender.base-url}",
    configuration = SmsSenderIntegrationConfiguration.class
)
public interface SmsSenderClient {

    @PostMapping("/send/sms")
    ResponseEntity<SendSmsResponse> sendSms(SendSmsRequest request);
}
