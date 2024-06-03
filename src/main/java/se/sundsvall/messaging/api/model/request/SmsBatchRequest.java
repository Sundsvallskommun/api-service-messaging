package se.sundsvall.messaging.api.model.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import se.sundsvall.dept44.common.validators.annotation.ValidMSISDN;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record SmsBatchRequest(

	@Schema(description = "Sender", accessMode = Schema.AccessMode.READ_ONLY) String sender,

	@Schema(description = "Origin of request", example = "web", hidden = true) @JsonIgnore String origin,

	@NotBlank @Schema(description = "Message to send as sms", requiredMode = REQUIRED) String message,

	@NotEmpty @Schema(description = "Parties to send the sms message to", requiredMode = REQUIRED) List<@Valid Party> parties) {

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "SmsBatchRequestParty")
	public record Party(

		@ValidUuid(nullable = true) @Schema(description = "The message party id (optional)", example = "f427952b-247c-4d3b-b081-675a467b3619") String partyId,

		@ValidMSISDN @Schema(description = "Mobile number, which should start with +467x", requiredMode = REQUIRED) String mobileNumber) {
	}

}