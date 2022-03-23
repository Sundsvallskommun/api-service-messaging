package se.sundsvall.messaging.model.dto;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.MessageType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with", toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UndeliverableMessageDto {

    private final String batchId;
    private final String messageId;
    private final String partyId;
    private final String partyContact;
    private final String subject;
    private final String content;
    private final String senderName;
    private final String senderEmail;
    private final MessageStatus status;
    private final MessageType type;
}
