package se.sundsvall.messaging.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.With;

import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.model.ExternalReference;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@With
@Builder(setterPrefix = "with")
public record WebMessageRequest(

	@Valid
	@NotNull
	@Schema(description = "Party", requiredMode = REQUIRED)
	Party party,

	@NotBlank
	@Schema(description = "Message", requiredMode = REQUIRED)
	String message,

	@Schema(description = "Origin of request", example = "web", hidden = true)
	@JsonIgnore
	String origin,

	@Size(max = 10)
	@ArraySchema(schema = @Schema(implementation = Attachment.class), maxItems = 10)
	List<Attachment> attachments) {

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "WebMessageParty")
	public record Party(

		@ValidUuid
        @Schema(description = "The message party id", format="uuid", requiredMode = REQUIRED)
		String partyId,

		@Schema(description = "External references")
		List<@Valid ExternalReference> externalReferences) {
	}

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "WebMessageAttachment", description = "Attachment")
	public record Attachment(

		@Schema(description = "File name")
		String fileName,

		@Schema(description = "Mime-type")
		String mimeType,

		@Schema(description = "BASE64-encoded file, max size 10 MB")
		String base64Data) {
	}
}
