package se.sundsvall.messaging.api.model.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import se.sundsvall.dept44.common.validators.annotation.OneOf;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.model.ExternalReference;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record DigitalMailRequest(

	@Valid
	@NotNull
	@Schema(description = "Party", requiredMode = REQUIRED)
	Party party,

	@Valid
	@Schema(description = "Sender")
	Sender sender,

	@Schema(description = "Subject", nullable = true)
	String subject,

	@Schema(description = "Department and unit that should be billed for the message", nullable = true,
		example = "SBK" + "(Gatuavdelningen, Trafiksektionen)")
	String department,

	@NotBlank
	@OneOf({"text/plain", "text/html"})
	@Schema(description = "Content type", allowableValues = {"text/plain", "text/html"})
	String contentType,

	@NotBlank
	@Schema(description = "Body (plain text if contentType is set to 'text/plain', BASE64-encoded if contentType is set to 'application/html')")
	String body,

	@Schema(description = "Origin of request", example = "web", hidden = true)
	@JsonIgnore
	String origin,

	@Schema(description = "Attachments")
	List<@Valid Attachment> attachments) {

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "DigitalMailParty")
	public record Party(

		@NotEmpty
        @ArraySchema(schema = @Schema(description = "The message party ids", format = "uuid"), minItems = 1)
		List<@ValidUuid String> partyIds,

		@Schema(description = "External references")
		List<@Valid ExternalReference> externalReferences) {
	}

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "DigitalMailSender")
	public record Sender(

		@Valid
		@NotNull
		SupportInfo supportInfo) {

		@With
		@Builder(setterPrefix = "with")
		@Schema(name = "DigitalMailSenderSupportInfo", description = "Support info")
		public record SupportInfo(

			@NotBlank
			@Schema(description = "Text", requiredMode = REQUIRED)
			String text,

			@jakarta.validation.constraints.Email
			@Schema(description = "E-mail address")
			String emailAddress,

			@Schema(description = "Phone number")
			String phoneNumber,

			@Schema(description = "URL")
			String url) {
		}
	}

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "DigitalMailAttachment", description = "Attachment")
	public record Attachment(

		@OneOf("application/pdf")
		@Schema(description = "Content type", allowableValues = {"application/pdf"})
		String contentType,

		@NotBlank
		@Schema(description = "Content (BASE64-encoded)")
		String content,

		@NotBlank
		@Schema(description = "Filename")
		String filename) {
	}
}
