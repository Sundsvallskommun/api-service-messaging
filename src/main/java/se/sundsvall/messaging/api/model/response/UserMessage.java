package se.sundsvall.messaging.api.model.response;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;

@Builder(setterPrefix = "with")
@Schema(description = "User message model")
public record UserMessage(

	@Schema(description = "The message id", example = "b971e0f8-2942-4b45-9fa3-bd2cc22ed76b")
	String messageId,

	@Schema(description = "The message issuer", example = "and06sod")
	String issuer,

	@Schema(description = "The system that the message originated from", example = "CASEDATA")
	String origin,

	@Schema(description = "When the message was sent", example = "2021-01-01T12:00:00")
	LocalDateTime sent,

	@ArraySchema(schema = @Schema(implementation = Recipient.class, accessMode = READ_ONLY))
	List<Recipient> recipients,

	@ArraySchema(schema = @Schema(implementation = MessageAttachment.class, accessMode = READ_ONLY))
	List<MessageAttachment> attachments) {

	@With
	@Builder(setterPrefix = "with")
	@Schema(description = "Recipient model")
	public record Recipient(

		@Schema(description = "The person identifier", example = "199001011234")
		String personId,

		@Schema(description = "The message type", example = "SNAIL_MAIL")
		String messageType,

		@Schema(description = "The message status", example = "SENT")
		String status) {
	}

	@With
	@Builder(setterPrefix = "with")
	@Schema(description = "Message attachment model")
	public record MessageAttachment(

		@Schema(description = "The attachment content type", example = "application/pdf")
		String contentType,

		@Schema(description = "The attachment file name", example = "attachment.pdf")
		String fileName) {
	}


}