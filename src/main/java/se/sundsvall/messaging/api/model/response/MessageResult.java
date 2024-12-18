package se.sundsvall.messaging.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder(setterPrefix = "with")
@Schema(description = "Message result")
public record MessageResult(

	@Schema(description = "The message id", format = "uuid") String messageId,

	@Schema(description = "The message deliveries") List<DeliveryResult> deliveries) {}
