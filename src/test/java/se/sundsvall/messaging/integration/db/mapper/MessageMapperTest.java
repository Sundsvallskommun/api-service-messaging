package se.sundsvall.messaging.integration.db.mapper;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.Message;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

class MessageMapperTest {

	@Test
	void mapToMessageWhenMessageEntityIsNull() {
		assertThat(MessageMapper.mapToMessage(null)).isNull();
	}

	@Test
	void mapToMessage() {
		final var messageEntity = MessageEntity.builder()
			.withBatchId("someBatchId")
			.withMessageId("someMessageId")
			.withDeliveryId("someDeliveryId")
			.withPartyId("somePartyId")
			.withType(SNAIL_MAIL)
			.withStatus(FAILED)
			.withContent("someContent")
			.withOrigin("someOrigin")
			.build();

		final var message = MessageMapper.mapToMessage(messageEntity);

		assertThat(message).isNotNull();
		assertThat(message.batchId()).isEqualTo(messageEntity.getBatchId());
		assertThat(message.messageId()).isEqualTo(messageEntity.getMessageId());
		assertThat(message.deliveryId()).isEqualTo(messageEntity.getDeliveryId());
		assertThat(message.partyId()).isEqualTo(messageEntity.getPartyId());
		assertThat(message.type()).isEqualTo(messageEntity.getType());
		assertThat(message.status()).isEqualTo(messageEntity.getStatus());
		assertThat(message.content()).isEqualTo(messageEntity.getContent());
		assertThat(message.origin()).isEqualTo(messageEntity.getOrigin());
	}

	@Test
	void mapToMessageEntityWhenMessageIsNull() {
		assertThat(MessageMapper.mapToMessageEntity(null)).isNull();
	}

	@Test
	void mapToMessageEntity() {
		final var message = Message.builder()
			.withBatchId("someBatchId")
			.withMessageId("someMessageId")
			.withDeliveryId("someDeliveryId")
			.withPartyId("somePartyId")
			.withType(SNAIL_MAIL)
			.withStatus(FAILED)
			.withContent("someContent")
			.withOrigin("someOrigin")
			.build();

		final var messageEntity = MessageMapper.mapToMessageEntity(message);

		assertThat(messageEntity).isNotNull();
		assertThat(messageEntity.getBatchId()).isEqualTo(message.batchId());
		assertThat(messageEntity.getMessageId()).isEqualTo(message.messageId());
		assertThat(messageEntity.getDeliveryId()).isEqualTo(message.deliveryId());
		assertThat(messageEntity.getPartyId()).isEqualTo(message.partyId());
		assertThat(messageEntity.getType()).isEqualTo(message.type());
		assertThat(messageEntity.getStatus()).isEqualTo(message.status());
		assertThat(messageEntity.getContent()).isEqualTo(message.content());
		assertThat(messageEntity.getOrigin()).isEqualTo(message.origin());
	}
}
