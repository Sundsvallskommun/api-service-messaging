package se.sundsvall.messaging.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.With;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@With
@Builder(setterPrefix = "with")
public record SlackRequest(

	@NotBlank @Schema(description = "App/bot token", requiredMode = REQUIRED) String token,

	@NotBlank @Schema(description = "Channel name/id", requiredMode = REQUIRED) String channel,

	@Schema(description = "Origin of request", example = "web", hidden = true) @JsonIgnore String origin,

	@Schema(description = "Issuer of request", example = "user123", hidden = true) @JsonIgnore String issuer,

	@NotBlank @Schema(description = "Message (supports Slack markdown formatting)", requiredMode = REQUIRED) String message,

	@Schema(description = "Municipality Id", hidden = true) @JsonIgnore String municipalityId) {
}
