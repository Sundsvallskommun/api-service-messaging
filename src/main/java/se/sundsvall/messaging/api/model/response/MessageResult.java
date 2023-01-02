package se.sundsvall.messaging.api.model.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import se.sundsvall.messaging.model.MessageStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(setterPrefix = "with", builderClassName = "Builder")
@JsonDeserialize(builder = MessageResult.Builder.class) // FOR APP TESTS
@Schema(description = "Message result")
public record MessageResult(

    @Schema(description = "The message id", format = "uuid")
    String messageId,

    @Schema(description = "The delivery id", nullable = true, format = "uuid")
    String deliveryId,

    @Schema(description = "Status", enumAsRef = true)
    MessageStatus status) {  }

