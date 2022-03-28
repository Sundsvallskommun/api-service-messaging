package se.sundsvall.messaging.api.response;

import java.time.LocalDateTime;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Party;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder(setterPrefix = "with")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HistoryResponse {

    private final String sender;
    private final Party party;
    private final MessageType messageType;
    private final String message;
    private final MessageStatus status;
    private final LocalDateTime timestamp;
}
