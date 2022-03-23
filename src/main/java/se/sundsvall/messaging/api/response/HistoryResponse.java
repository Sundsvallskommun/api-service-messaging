package se.sundsvall.messaging.api.response;

import java.time.LocalDateTime;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.MessageType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder(setterPrefix = "with")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HistoryResponse {

    private final String sender;
    private final String partyId;
    private final MessageType messageType;
    private final String message;
    private final MessageStatus status;
    private final LocalDateTime timestamp;
}
