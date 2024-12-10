package se.sundsvall.messaging.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder(setterPrefix = "with")
@Schema(description = "Message batch result")
public record MessageBatchResult(

	@Schema(description = "The batch id", format = "uuid") String batchId,

	@Schema(description = "The individual message results") List<MessageResult> messages) {}
