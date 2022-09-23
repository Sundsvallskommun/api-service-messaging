package se.sundsvall.messaging.api.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with", builderClassName = "Builder")
@JsonDeserialize(builder = HistoryResponse.Builder.class) // FOR TESTS
@JsonPropertyOrder({"content", "messageType", "status", "timestamp"})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HistoryResponse {

    private final MessageType messageType;
    private final MessageStatus status;
    private final Object content;
    private final LocalDateTime timestamp;
}
