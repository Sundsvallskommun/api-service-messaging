package se.sundsvall.messaging.api.model.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import se.sundsvall.messaging.model.Header;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record SlackRequest(

        @Schema(description = "Headers")
        List<@Valid Header> headers,

        @NotBlank
        @Schema(description = "App/bot token", requiredMode = REQUIRED)
        String token,

        @NotBlank
        @Schema(description = "Channel name/id", requiredMode = REQUIRED)
        String channel,

        @NotBlank
        @Schema(description = "Message (supports Slack markdown formatting)", requiredMode = REQUIRED)
        String message) { }
