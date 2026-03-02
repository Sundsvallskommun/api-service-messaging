package se.sundsvall.messaging.api.model.response;

import java.time.LocalDateTime;
import lombok.Builder;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import tools.jackson.databind.annotation.JsonDeserialize;

@Builder(setterPrefix = "with", builderClassName = "Builder")
@JsonDeserialize(builder = HistoryResponse.Builder.class) // FOR TESTS
public record HistoryResponse(MessageType messageType, MessageStatus status, Object content, LocalDateTime timestamp) {
}
