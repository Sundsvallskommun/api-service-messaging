package se.sundsvall.messaging.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.With;

@Builder(setterPrefix = "with")
@Schema(description = "Batch information model")
public record Batch(
	@Schema(description = "The batch id", examples = "b971e0f8-2942-4b45-9fa3-bd2cc22ed76b") String batchId,
	@Schema(description = "The original message type", examples = "LETTER") String messageType,
	@Schema(description = "Message subject if such exists for message(s) attached to the batch", examples = "Important message") String subject,
	@Schema(description = "Timestamp when the batch was sent", examples = "2021-01-01T12:00:00") LocalDateTime sent,
	@Schema(description = "The amount of documents attached to message(s) in the batch", examples = "3") int attachmentCount,
	@Schema(description = "The amount of recipients included in the batch", examples = "15") int recipientCount,
	@Schema(implementation = Status.class) Status status) {

	@With
	@Builder(setterPrefix = "with")
	@Schema(description = "Batch status model")
	public record Status(
		@Schema(description = "Amount of successfully sent messages", examples = "13") int successful,
		@Schema(description = "Amount of failed messages", examples = "2") int unsuccessful) {}
}
