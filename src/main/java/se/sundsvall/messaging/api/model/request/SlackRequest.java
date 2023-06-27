package se.sundsvall.messaging.api.model.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record SlackRequest(

        @NotBlank
        @Schema(description = "App/bot token", requiredMode = REQUIRED)
        String token,

        @NotBlank
        @Schema(description = "Channel name/id", requiredMode = REQUIRED)
        String channel,

        @NotBlank
        @Schema(description = "Message (supports Slack markdown formatting)", requiredMode = REQUIRED)
        String message) { }
