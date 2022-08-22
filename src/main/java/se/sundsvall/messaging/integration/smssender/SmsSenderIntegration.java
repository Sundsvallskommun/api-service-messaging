package se.sundsvall.messaging.integration.smssender;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.dto.SmsDto;

import generated.se.sundsvall.smssender.SendSmsResponse;

@Component
public class SmsSenderIntegration {

    static final String INTEGRATION_NAME = "SmsSender";

    private final SmsSenderClient client;
    private final SmsSenderIntegrationMapper mapper;

    public SmsSenderIntegration(final SmsSenderClient client, final SmsSenderIntegrationMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public ResponseEntity<SendSmsResponse> sendSms(final SmsDto smsDto) {
        var request = mapper.toSendSmsRequest(smsDto);

        return client.sendSms(request);
    }
}
