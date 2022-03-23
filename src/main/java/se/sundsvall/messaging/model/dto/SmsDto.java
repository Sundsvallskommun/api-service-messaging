package se.sundsvall.messaging.model.dto;

import se.sundsvall.messaging.api.MessageStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder(setterPrefix = "with")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SmsDto {

    private final String batchId;
    private final String messageId;
    private final String sender;
    private final String partyId;
    private final String mobileNumber;
    private final String message;
    private final MessageStatus status;
}
