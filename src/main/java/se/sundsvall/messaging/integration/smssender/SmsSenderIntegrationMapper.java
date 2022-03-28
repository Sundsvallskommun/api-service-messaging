package se.sundsvall.messaging.integration.smssender;

import org.springframework.stereotype.Component;

import se.sundsvall.messaging.dto.SmsDto;

import generated.se.sundsvall.smssender.SmsRequest;

@Component
class SmsSenderIntegrationMapper {

    SmsRequest toRequest(final SmsDto smsDto) {
        if (smsDto == null) {
            return null;
        }

        return new SmsRequest()
            .message(smsDto.getMessage())
            .mobileNumber(smsDto.getMobileNumber())
            .sender(smsDto.getSender());
    }
}
