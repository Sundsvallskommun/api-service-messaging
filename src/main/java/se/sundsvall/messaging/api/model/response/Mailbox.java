package se.sundsvall.messaging.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(setterPrefix = "with")
@Schema(description = "Response model for a mailbox")
public record Mailbox(

	@Schema(description = "partyId for the person the mailbox belongs to") String partyId,

	@Schema(description = "Name of the mailbox, e.g. Kivra") String supplier,

	@Schema(description = "If it's possible to send messages to this mailbox") boolean reachable) {}
