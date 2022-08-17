package se.sundsvall.messaging.integration.smssender;

import org.springframework.stereotype.Component;

import se.sundsvall.messaging.dto.SmsDto;

import generated.se.sundsvall.smssender.SendSmsRequest;
import generated.se.sundsvall.smssender.Sender;

@Component
class SmsSenderIntegrationMapper {

    SendSmsRequest toSendSmsRequest(final SmsDto smsDto) {
        if (smsDto == null) {
            return null;
        }

        return new SendSmsRequest()
            .sender(new Sender()
                .name(smsDto.getSender().getName()))
            .mobileNumber(smsDto.getMobileNumber())
            .message(smsDto.getMessage());
    }
}
