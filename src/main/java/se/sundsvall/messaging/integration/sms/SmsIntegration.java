package se.sundsvall.messaging.integration.sms;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import se.sundsvall.messaging.integration.AbstractRestIntegration;

import generated.se.sundsvall.smssender.SmsRequest;

@Component
public class SmsIntegration extends AbstractRestIntegration {

    private final RestTemplate restTemplate;
    private final SmsIntegrationProperties smsProperties;

    public SmsIntegration(@Qualifier("integration.sms-sender.resttemplate") RestTemplate restTemplate, SmsIntegrationProperties smsProperties) {
        this.restTemplate = restTemplate;
        this.smsProperties = smsProperties;
    }

    public int getMessageRetries() {
        return smsProperties.getMessageRetries();
    }

    public ResponseEntity<Boolean> sendSms(SmsRequest sms) {
        return restTemplate.postForEntity("/send/sms", new HttpEntity<>(sms, createHeaders()), Boolean.class);
    }
}
