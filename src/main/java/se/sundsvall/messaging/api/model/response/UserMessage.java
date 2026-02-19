package se.sundsvall.messaging.api.model.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.With;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Builder(setterPrefix = "with")
@Schema(description = "User message model")
public record UserMessage(

	@Schema(description = "The message id", examples = "b971e0f8-2942-4b45-9fa3-bd2cc22ed76b") String messageId,

	@Schema(description = "The message issuer", examples = "and06sod") String issuer,

	@Schema(description = "The system that the message originated from", examples = "CASEDATA") String origin,

	@Schema(description = "When the message was sent", examples = "2021-01-01T12:00:00") LocalDateTime sent,

	@Schema(description = "The message subject", examples = "Important message") String subject,

	@Schema(description = "The message body", examples = "This is a message") String body,

	@ArraySchema(schema = @Schema(implementation = Recipient.class, accessMode = READ_ONLY)) List<Recipient> recipients,

	@ArraySchema(schema = @Schema(implementation = MessageAttachment.class, accessMode = READ_ONLY)) List<MessageAttachment> attachments) {

	@With
	@Builder(setterPrefix = "with")
	@Schema(description = "Recipient model")
	public record Recipient(

		@Schema(description = "The recipient address") Address address,

		@Schema(description = "The person identifier", examples = "199001011234") String personId,

		@Schema(description = "The message type", examples = "SNAIL_MAIL") String messageType,

		@Schema(description = "The recipient mobile number", examples = "+46123456789") String mobileNumber,

		@Schema(description = "The message status", examples = "SENT") String status) {
	}

	@With
	@Builder(setterPrefix = "with")
	@Schema(description = "Message attachment model")
	public record MessageAttachment(

		@Schema(description = "The attachment content type", examples = "application/pdf") String contentType,

		@Schema(description = "The attachment file name", examples = "attachment.pdf") String fileName) {
	}

	@With
	@Builder(setterPrefix = "with")
	@Schema(description = "Address model")
	public record Address(
		@Schema(description = "The address", examples = "Storgatan 1") String address,
		@Schema(description = "The city", examples = "Sundsvall") String city,
		@Schema(description = "The country", examples = "Sweden") String country,
		@Schema(description = "The first name", examples = "Kalle") String firstName,
		@Schema(description = "The last name", examples = "Kula") String lastName,
		@Schema(description = "The care of", examples = "c/o Name Namesson") String careOf,
		@Schema(description = "The zip code", examples = "123 45") String zipCode) {
	}

}
