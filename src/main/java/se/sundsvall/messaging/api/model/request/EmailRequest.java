package se.sundsvall.messaging.api.model.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.With;
import se.sundsvall.dept44.common.validators.annotation.ValidBase64;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.api.validation.ValidHeaders;
import se.sundsvall.messaging.model.ExternalReference;

@With
@Builder(setterPrefix = "with")
public record EmailRequest(

	@Valid @Schema(description = "Party") Party party,

	@Email @NotBlank @Schema(description = "Recipient e-mail address", requiredMode = REQUIRED) String emailAddress,

	@NotBlank @Schema(description = "E-mail subject", requiredMode = REQUIRED) String subject,

	@Schema(description = "E-mail plain-text body") String message,

	@ValidBase64(nullable = true) @Schema(description = "E-mail HTML body (BASE64-encoded)") String htmlMessage,

	@Valid @Schema(description = "Sender") Sender sender,

	@Schema(description = "Origin of request", example = "web", hidden = true) @JsonIgnore String origin,

	@Schema(description = "Issuer of request", example = "user123", hidden = true) @JsonIgnore String issuer,

	@ArraySchema(schema = @Schema(implementation = Attachment.class)) List<@Valid Attachment> attachments,

	@Schema(description = "Headers") @ValidHeaders Map<@NotBlank String, @NotEmpty List<String>> headers,

	@Schema(description = "Municipality Id", hidden = true) @JsonIgnore String municipalityId) {

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "EmailRequestParty")
	public record Party(

		@ValidUuid(nullable = true) @Schema(description = "The message party id", format = "uuid") String partyId,

		@Schema(description = "External references") List<@Valid ExternalReference> externalReferences) {
	}

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "EmailSender", description = "Sender")
	public record Sender(

		@NotBlank @Schema(description = "The sender of the e-mail") String name,

		@Email @NotBlank @Schema(description = "Sender e-mail address", example = "sender@sender.se") String address,

		@Email @Schema(description = "Reply-to e-mail address", example = "sender@sender.se") String replyTo) {
	}

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "EmailAttachment", description = "Attachment")
	public record Attachment(

		@NotBlank @Schema(description = "The attachment filename", example = "test.txt", requiredMode = REQUIRED) String name,

		@Schema(description = "The attachment content type", example = "text/plain") String contentType,

		@ValidBase64 @Schema(description = "The attachment (file) content as a BASE64-encoded string", example = "aGVsbG8gd29ybGQK", requiredMode = REQUIRED) String content) {
	}
}
