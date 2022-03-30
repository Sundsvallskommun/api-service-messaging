package se.sundsvall.messaging.api.model;

import java.time.LocalDateTime;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HistoryResponse {

    private final MessageType messageType;
    private final MessageStatus status;
    private final Object content;
    private final LocalDateTime timestamp;
}
