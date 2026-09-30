package se.sundsvall.messaging.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import tools.jackson.databind.annotation.JsonDeserialize;

@Builder(setterPrefix = "with")
@JsonDeserialize(builder = HistoryResponse.HistoryResponseBuilder.class) // FOR TESTS
public record HistoryResponse(
	@Schema(enumAsRef = true) MessageType messageType,
	@Schema(enumAsRef = true) MessageStatus status,
	Object content,
	LocalDateTime timestamp) {}
