package se.sundsvall.messaging.model;

import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record Message(
	String batchId,
	String messageId,
	String deliveryId,
	String partyId,
	MessageType type,
	MessageType originalType,
	MessageStatus status,
	Address address,
	String content,
	String origin,
	String issuer,
	String municipalityId) {}
