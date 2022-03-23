package se.sundsvall.messaging.model.dto;

import java.time.LocalDateTime;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.MessageType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with", toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HistoryDto {
    
    private final String id;
    private final String batchId;
    private final String messageId;
    private final MessageType messageType;
    private final String sender;
    private final String partyId;
    private final String partyContact;
    private final String message;
    private final MessageStatus status;
    private final LocalDateTime createdAt;
}
