package se.sundsvall.messaging.api.model.response;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(setterPrefix = "with", builderClassName = "Builder")
@JsonDeserialize(builder = MessageBatchResult.Builder.class) // FOR APP TESTS
@Schema(description = "Message batch result")
public record MessageBatchResult(

    @Schema(description = "The batch id", format = "uuid")
    String batchId,

    @Schema(description = "The individual message results")
    List<MessageResult> messages) {  }

