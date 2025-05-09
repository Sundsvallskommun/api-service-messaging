package se.sundsvall.messaging.api.model.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.With;
import se.sundsvall.dept44.common.validators.annotation.ValidMSISDN;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.model.ExternalReference;

@With
@Builder(setterPrefix = "with")
public record SmsRequest(

	@Valid @Schema(description = "Party") Party party,

	@Schema(description = "The sender of the SMS, swedish letters(å,ä,ö) will be replaced by (a,a,o) respectively", maxLength = 11, minLength = 3, example = "sender") @Size(max = 11, min = 3) String sender,

	@ValidMSISDN @Schema(description = "Mobile number. Should start with +467x", requiredMode = REQUIRED) String mobileNumber,

	@Schema(description = "Origin of request", example = "web", hidden = true) @JsonIgnore String origin,

	@Schema(description = "Issuer of request", example = "user123", hidden = true) @JsonIgnore String issuer,

	@NotBlank @Schema(description = "Message", requiredMode = REQUIRED) String message,

	@Schema(description = "Priority (optional, will be defaulted to NORMAL if not present)") Priority priority,

	@Schema(description = "Department", example = "API-Team") String department,

	@Schema(description = "Municipality Id", hidden = true) @JsonIgnore String municipalityId) {

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "SmsRequestParty")
	public record Party(

		@ValidUuid(nullable = true) @Schema(description = "The message party id", example = "f427952b-247c-4d3b-b081-675a467b3619") String partyId,

		@Schema(description = "External references") List<@Valid ExternalReference> externalReferences) {
	}
}
