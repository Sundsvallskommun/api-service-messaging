package se.sundsvall.messaging.api.model.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;
import lombok.With;
import se.sundsvall.dept44.common.validators.annotation.ValidBase64;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.model.Address;
import se.sundsvall.messaging.model.ExternalReference;

@With
@Builder(setterPrefix = "with")
@Schema(name = "SnailmailRequest")
public record SnailMailRequest(

	@Valid @Schema(description = "Party") Party party,

	@Schema(description = "Address") Address address,

	@NotBlank @Schema(description = "Department and unit that should be billed", example = "SBK(Gatuavdelningen, Trafiksektionen)", requiredMode = REQUIRED) String department,

	@Schema(description = "If the letter to send deviates from the standard", example = "A3 Ritning") String deviation,

	@Schema(description = "Origin of request", example = "web", hidden = true) String origin,

	@Schema(description = "Issuer of request", example = "user123", hidden = true) String issuer,

	@ArraySchema(schema = @Schema(implementation = Attachment.class), minItems = 1) List<@Valid Attachment> attachments) {

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "SnailmailParty")
	public record Party(

		@ValidUuid(nullable = true) @Schema(description = "The message party id", example = "f427952b-247c-4d3b-b081-675a467b3619") String partyId,

		@Schema(description = "External references") List<@Valid ExternalReference> externalReferences) {
	}

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "SnailmailAttachment", description = "Attachment")
	public record Attachment(

		@NotBlank @Schema(description = "The attachment filename", example = "test.txt", requiredMode = REQUIRED) String name,

		@Schema(description = "The attachment content type", example = "text/plain") String contentType,

		@ValidBase64 @Schema(description = "The attachment (file) content as a BASE64-encoded string", example = "aGVsbG8gd29ybGQK", requiredMode = REQUIRED) String content) {
	}
}
