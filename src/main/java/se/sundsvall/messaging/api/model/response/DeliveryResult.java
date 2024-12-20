package se.sundsvall.messaging.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

@Builder(setterPrefix = "with")
@Schema(description = "Delivery result")
public record DeliveryResult(

	@Schema(description = "The delivery id", format = "uuid") String deliveryId,

	@Schema(description = "Message type", enumAsRef = true) MessageType messageType,

	@Schema(description = "Status", enumAsRef = true) MessageStatus status) {}
