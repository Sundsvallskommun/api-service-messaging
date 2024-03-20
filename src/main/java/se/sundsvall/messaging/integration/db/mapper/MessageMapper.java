package se.sundsvall.messaging.integration.db.mapper;

import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.Message;

public class MessageMapper {
	private MessageMapper() {}

	public static Message mapToMessage(final MessageEntity messageEntity) {
		if (null == messageEntity) {
			return null;
		}

		return Message.builder()
			.withBatchId(messageEntity.getBatchId())
			.withMessageId(messageEntity.getMessageId())
			.withDeliveryId(messageEntity.getDeliveryId())
			.withPartyId(messageEntity.getPartyId())
			.withType(messageEntity.getType())
			.withOriginalType(messageEntity.getOriginalMessageType())
			.withStatus(messageEntity.getStatus())
			.withContent(messageEntity.getContent())
			.build();
	}

	public static MessageEntity mapToMessageEntity(final String origin, final Message message) {
		if (null == message) {
			return null;
		}

		return MessageEntity.builder()
			.withBatchId(message.batchId())
			.withMessageId(message.messageId())
			.withDeliveryId(message.deliveryId())
			.withPartyId(message.partyId())
			.withType(message.type())
			.withOriginalMessageType(message.originalType())
			.withStatus(message.status())
			.withContent(message.content())
			.withOrigin(origin)
			.build();
	}
}
