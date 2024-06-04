package se.sundsvall.messaging.api.model.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.With;
import se.sundsvall.dept44.common.validators.annotation.ValidMSISDN;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.model.ExternalReference;

@With
@Builder(setterPrefix = "with")
public record SmsRequest(

	@Valid @Schema(description = "Party") Party party,

	@Schema(description = "Sender") String sender,

	@ValidMSISDN @Schema(description = "Mobile number. Should start with +467x", requiredMode = REQUIRED) String mobileNumber,

	@Schema(description = "Origin of request", example = "web", hidden = true) @JsonIgnore String origin,

	@NotBlank @Schema(description = "Message", requiredMode = REQUIRED) String message,

	@Schema(description = "Priority (optional, will be defaulted to NORMAL if not present)") Priority priority) {

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "SmsRequestParty")
	public record Party(

		@ValidUuid(nullable = true) @Schema(description = "The message party id", example = "f427952b-247c-4d3b-b081-675a467b3619") String partyId,

		@Schema(description = "External references") List<@Valid ExternalReference> externalReferences) {
	}
}
