package se.sundsvall.messaging.dto;

import se.sundsvall.messaging.model.Sender;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SmsDto {

    private final Sender.Sms sender;
    private final String mobileNumber;
    private final String message;
}
