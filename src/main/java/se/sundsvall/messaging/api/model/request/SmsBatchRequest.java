package se.sundsvall.messaging.api.model.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.With;
import se.sundsvall.dept44.common.validators.annotation.ValidMSISDN;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@With
@Builder(setterPrefix = "with")
public record SmsBatchRequest(

	@Schema(description = "The sender of the SMS, swedish letters(å,ä,ö) will be replaced by (a,a,o) respectively", maxLength = 11, minLength = 3, examples = "sender") @Size(max = 11, min = 3) String sender,

	@Schema(description = "Origin of request", examples = "web", hidden = true) @JsonIgnore String origin,

	@Schema(description = "Issuer of request", examples = "user123", hidden = true) @JsonIgnore String issuer,

	@NotBlank @Schema(description = "Message to send as sms", requiredMode = REQUIRED) String message,

	@Schema(description = "Priority (optional, will be defaulted to NORMAL if not present)") Priority priority,

	@Schema(description = "Department", examples = "API-Team") String department,

	@NotEmpty @Schema(description = "Parties to send the sms message to", requiredMode = REQUIRED) List<@Valid Party> parties,

	@Schema(description = "Municipality Id", hidden = true) @JsonIgnore String municipalityId) {

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "SmsBatchRequestParty")
	public record Party(

		@ValidUuid(nullable = true) @Schema(description = "The message party id (optional)", examples = "f427952b-247c-4d3b-b081-675a467b3619") String partyId,

		@ValidMSISDN @Schema(description = "Mobile number, which should start with +467x", requiredMode = REQUIRED) String mobileNumber) {
	}

}
