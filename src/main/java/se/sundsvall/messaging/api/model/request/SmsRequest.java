package se.sundsvall.messaging.api.model.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

import se.sundsvall.dept44.common.validators.annotation.ValidMSISDN;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.model.ExternalReference;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record SmsRequest(

	@Valid @Schema(description = "Party") Party party,

	@Schema(description = "The sender of the SMS, must be between 3-11 characters and start with a non-numeric character",
		requiredMode = REQUIRED, maxLength = 11, minLength = 3, example = "sender")
	@Pattern(regexp = "^[a-zA-Z ][a-zA-Z0-9 ]{2,10}$", message = "sender must be between 3-11 characters and start with a non-numeric character")
	String sender,

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
