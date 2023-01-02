package se.sundsvall.messaging.integration.smssender;

import org.springframework.stereotype.Component;

import generated.se.sundsvall.smssender.SendSmsRequest;
import generated.se.sundsvall.smssender.Sender;

@Component
class SmsSenderIntegrationMapper {

    SendSmsRequest toSendSmsRequest(final SmsDto dto) {
        if (dto == null) {
            return null;
        }

        return new SendSmsRequest()
            .sender(new Sender().name(dto.sender()))
            .mobileNumber(dto.mobileNumber())
            .message(dto.message());
    }
}
