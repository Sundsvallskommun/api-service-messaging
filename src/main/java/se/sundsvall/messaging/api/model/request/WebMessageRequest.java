package se.sundsvall.messaging.api.model.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.With;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.api.validation.ValidInstance;
import se.sundsvall.messaging.api.validation.ValidWebMessageRequest;
import se.sundsvall.messaging.model.ExternalReference;

@With
@Builder(setterPrefix = "with")
@ValidWebMessageRequest
public record WebMessageRequest(

	@Valid @NotNull @Schema(description = "Party", requiredMode = REQUIRED) Party party,

	@NotBlank @Schema(description = "Message", requiredMode = REQUIRED) String message,

	@Schema(description = "The sender", requiredMode = NOT_REQUIRED) Sender sender,

	@Schema(description = "Send as owner", requiredMode = NOT_REQUIRED, defaultValue = "false") boolean sendAsOwner,

	@Schema(description = "Origin of request", example = "web", hidden = true) @JsonIgnore String origin,

	@Schema(description = "Issuer of request", example = "user123", hidden = true) @JsonIgnore String issuer,

	@Schema(description = "Determines if the message should be added to the internal or external OeP instance", allowableValues = {
		"internal", "external"
	}, example = "internal") @ValidInstance(nullable = true) String oepInstance,

	@Size(max = 10) @ArraySchema(schema = @Schema(implementation = Attachment.class), maxItems = 10) List<Attachment> attachments,

	@Schema(description = "Municipality Id", hidden = true) @JsonIgnore String municipalityId){

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "WebMessageParty")
	public record Party(

		@ValidUuid(nullable = true) @Schema(description = "The message party id", format = "uuid", requiredMode = NOT_REQUIRED) String partyId,

		@Schema(description = "External references") List<@Valid ExternalReference> externalReferences) {
	}

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "WebMessageAttachment", description = "Attachment")
	public record Attachment(

		@Schema(description = "File name") String fileName,

		@Schema(description = "Mime-type") String mimeType,

		@Schema(description = "BASE64-encoded file, max size 50 MB") String base64Data) {
	}

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "WebMessageSender", description = "Sender", requiredMode = NOT_REQUIRED)
	public record Sender(

		@Schema(description = "The user ID of the sender. I.e. employee ID", example = "joe01doe") String userId) {
	}
}
