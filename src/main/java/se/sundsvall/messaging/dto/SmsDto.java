package se.sundsvall.messaging.dto;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.Party;

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
    private final Party party;
    private final String mobileNumber;
    private final String message;
    private final MessageStatus status;
}
