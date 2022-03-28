package se.sundsvall.messaging.api.model;

import java.time.LocalDateTime;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Party;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HistoryResponse {

    private final String sender;
    private final Party party;
    private final MessageType messageType;
    private final String message;
    private final MessageStatus status;
    private final LocalDateTime timestamp;
}
