package se.sundsvall.messaging.model;

import se.sundsvall.messaging.api.model.request.EnvelopeType;

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
    String content,
	EnvelopeType envelopeType) {  }
