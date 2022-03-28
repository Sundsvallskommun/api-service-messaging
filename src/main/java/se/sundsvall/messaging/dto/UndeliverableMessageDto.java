package se.sundsvall.messaging.dto;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Party;

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
    private final Party party;
    private final String partyContact;
    private final String subject;
    private final String content;
    private final String senderName;
    private final String senderEmail;
    private final MessageStatus status;
    private final MessageType type;
}
