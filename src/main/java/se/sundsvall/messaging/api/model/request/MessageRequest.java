package se.sundsvall.messaging.api.model.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.With;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.model.ExternalReference;

@With
@Builder(setterPrefix = "with")
public record MessageRequest(

	@Schema(description = "Origin of request", examples = "web", hidden = true) @JsonIgnore String origin,

	@Schema(description = "Issuer of request", examples = "user123", hidden = true) @JsonIgnore String issuer,

	@NotEmpty @Schema(description = "The messages to be sent", requiredMode = REQUIRED) List<@Valid Message> messages,

	@Schema(description = "Municipality Id", hidden = true) @JsonIgnore String municipalityId) {

	@With
	@Builder(setterPrefix = "with")
	public record Message(

		@Valid @NotNull @Schema(description = "Party", requiredMode = REQUIRED) Party party,

		@Schema(description = "Filters", examples = "{\"someAttributeName\": [\"someAttributeValue\"]}") Map<String, List<String>> filters,

		@Valid @Schema(description = "Sender") Sender sender,

		@Schema(description = "The message subject (for E-mails)") String subject,

		@NotBlank @Schema(description = "Plain-text message text", requiredMode = REQUIRED) String message,

		@Schema(description = "HTML message text, for e-mails (BASE64-encoded)") String htmlMessage) {

		@With
		@Builder(setterPrefix = "with")
		@Schema(name = "MessageParty")
		public record Party(

			@ValidUuid @Schema(description = "The message party id", format = "uuid", requiredMode = REQUIRED) String partyId,

			@Schema(description = "External references") List<@Valid ExternalReference> externalReferences) {
		}

		@With
		@Builder(setterPrefix = "with")
		@Schema(name = "MessageSender")
		public record Sender(

			@Valid Email email,

			@Valid Sms sms) {

			@With
			@Builder(setterPrefix = "with")
			public record Email(

				@NotBlank @Schema(description = "The sender of the e-mail") String name,

				@jakarta.validation.constraints.Email @NotBlank @Schema(description = "Sender e-mail address", examples = "sender@sender.se") String address,

				@Schema(description = "Reply-to e-mail address", examples = "sender@sender.se") @jakarta.validation.constraints.Email String replyTo) {
			}

			@With
			@Builder(setterPrefix = "with")
			public record Sms(

				@NotBlank @Size(max = 11) @Schema(description = "The sender of the SMS", maxLength = 11, examples = "sender") String name) {
			}
		}
	}
}
