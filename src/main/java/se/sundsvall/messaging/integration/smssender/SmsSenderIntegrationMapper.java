package se.sundsvall.messaging.integration.smssender;

import org.springframework.stereotype.Component;

import se.sundsvall.messaging.dto.SmsDto;

import generated.se.sundsvall.smssender.SendSmsRequest;

@Component
class SmsSenderIntegrationMapper {

    SendSmsRequest toRequest(final SmsDto smsDto) {
        if (smsDto == null) {
            return null;
        }

        return new SendSmsRequest()
            .message(smsDto.getMessage())
            .mobileNumber(smsDto.getMobileNumber())
            .sender(smsDto.getSender());
    }
}
