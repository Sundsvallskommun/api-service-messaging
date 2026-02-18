package se.sundsvall.messaging.integration.db.mapper;

import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.Message;

import static java.util.Optional.ofNullable;

public final class MessageMapper {

	private MessageMapper() {}

	public static Message mapToMessage(final MessageEntity messageEntity) {
		return ofNullable(messageEntity).map(actualMessageEntity -> Message.builder()
			.withBatchId(actualMessageEntity.getBatchId())
			.withMessageId(actualMessageEntity.getMessageId())
			.withDeliveryId(actualMessageEntity.getDeliveryId())
			.withPartyId(actualMessageEntity.getPartyId())
			.withMunicipalityId(actualMessageEntity.getMunicipalityId())
			.withType(actualMessageEntity.getType())
			.withOriginalType(actualMessageEntity.getOriginalMessageType())
			.withStatus(actualMessageEntity.getStatus())
			.withContent(actualMessageEntity.getContent())
			.withOrigin(actualMessageEntity.getOrigin())
			.withIssuer(actualMessageEntity.getIssuer())
			.withAddress(messageEntity.getDestinationAddress())
			.withOrganizationNumber(actualMessageEntity.getOrganizationNumber())
			.build()).orElse(null);
	}

	public static MessageEntity mapToMessageEntity(final Message message) {
		return ofNullable(message).map(actualMessage -> MessageEntity.builder()
			.withBatchId(actualMessage.batchId())
			.withMessageId(actualMessage.messageId())
			.withDeliveryId(actualMessage.deliveryId())
			.withPartyId(actualMessage.partyId())
			.withMunicipalityId(actualMessage.municipalityId())
			.withType(actualMessage.type())
			.withOriginalMessageType(actualMessage.originalType())
			.withStatus(actualMessage.status())
			.withContent(actualMessage.content())
			.withOrigin(actualMessage.origin())
			.withIssuer(actualMessage.issuer())
			.withDestinationAddress(actualMessage.address())
			.withOrganizationNumber(actualMessage.organizationNumber())
			.build()).orElse(null);
	}
}
