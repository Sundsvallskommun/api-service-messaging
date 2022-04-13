package se.sundsvall.messaging.dto;

import java.time.LocalDateTime;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with", toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HistoryDto {
    
    private final String messageId;
    private final String batchId;
    private final MessageType messageType;
    private final MessageStatus status;
    private final String content;
    private final LocalDateTime createdAt;
}
