package se.sundsvall.messaging.integration.db.mapper;

import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.Message;

import java.util.Optional;

public class MessageMapper {
	private MessageMapper() {}

	public static Message mapToMessage(final MessageEntity messageEntity) {
		return Optional.ofNullable(messageEntity).map(entity -> Message.builder()
			.withBatchId(messageEntity.getBatchId())
			.withMessageId(messageEntity.getMessageId())
			.withDeliveryId(messageEntity.getDeliveryId())
			.withPartyId(messageEntity.getPartyId())
			.withType(messageEntity.getType())
			.withOriginalType(messageEntity.getOriginalMessageType())
			.withStatus(messageEntity.getStatus())
			.withContent(messageEntity.getContent())
			.withOrigin(messageEntity.getOrigin())
			.build()).orElse(null);
	}

	public static MessageEntity mapToMessageEntity(final Message message) {
		return Optional.ofNullable(message).map(message1 -> MessageEntity.builder()
			.withBatchId(message.batchId())
			.withMessageId(message.messageId())
			.withDeliveryId(message.deliveryId())
			.withPartyId(message.partyId())
			.withType(message.type())
			.withOriginalMessageType(message.originalType())
			.withStatus(message.status())
			.withContent(message.content())
			.withOrigin(message.origin())
			.build()).orElse(null);
	}
}
